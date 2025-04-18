package nmng108.microtube.processor.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.GetObjectResponse;
import jakarta.jms.Queue;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import nmng108.microtube.processor.configuration.ObjectStoreConfiguration;
import nmng108.microtube.processor.dto.base.ErrorCode;
import nmng108.microtube.processor.entity.Channel;
import nmng108.microtube.processor.entity.User;
import nmng108.microtube.processor.entity.Video;
import nmng108.microtube.processor.exception.*;
import nmng108.microtube.processor.model.MediaFileInfo;
import nmng108.microtube.processor.repository.ChannelRepository;
import nmng108.microtube.processor.repository.UserRepository;
import nmng108.microtube.processor.repository.VideoRepository;
import nmng108.microtube.processor.service.ProcessingService;
import nmng108.microtube.processor.service.UserService;
import nmng108.microtube.processor.util.Helper;
import nmng108.microtube.processor.util.constant.Constants;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Stream;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProcessingServiceImpl implements ProcessingService {
    private static final String MASTER_FILENAME = "master.m3u8";

    //    TaskScheduler videoCreationWaitScheduler;
//    ThreadPoolTaskExecutor ioTaskThreadPool;
//    Map<String, ScheduledFuture<?>> waitForCreatingVideoTasks = new HashMap<>();
//    Map<String, ScheduledFuture<?>> ioTasks = new HashMap<>();

    //    JmsTemplate jmsTemplate;
    Queue videoProcessingRequestQueue;
    ObjectMapper objectMapper;
    ObjectStoreConfiguration objectStoreConfiguration;
    MinioObjectStoreServiceImpl objectStoreService;
    UserService userService;
    UserRepository userRepository;
    ChannelRepository channelRepository;
    VideoRepository videoRepository;

    Path tmpOriginalFileDirectory;
    Path resultDirectory;

    public ProcessingServiceImpl(
//            JmsTemplate jmsTemplate,
            @Qualifier("videoProcessingRequestQueue") Queue videoProcessingRequestQueue,
            ObjectMapper objectMapper,
            ObjectStoreConfiguration objectStoreConfiguration,
            MinioObjectStoreServiceImpl objectStoreService,
            UserService userService,
            UserRepository userRepository,
            ChannelRepository channelRepository,
            VideoRepository videoRepository,
            @Value("${video.temp-dir}") String tmpOriginalFileDirectory,
            @Value("${video.result-dir}") String resultDirectory
    ) throws IOException {
//        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
//
//        scheduler.setThreadNamePrefix("video-creation-wait-thread-");
//        scheduler.setPoolSize(10);
//        scheduler.afterPropertiesSet();
//
//        videoCreationWaitScheduler = scheduler;
//        ioTaskThreadPool = new ThreadPoolTaskExecutor();
//
//        ioTaskThreadPool.setCorePoolSize(5);
//        ioTaskThreadPool.setMaxPoolSize(200);
//        ioTaskThreadPool.setQueueCapacity(500);

//        this.jmsTemplate = jmsTemplate;
        this.videoProcessingRequestQueue = videoProcessingRequestQueue;
        this.objectMapper = objectMapper;
        this.objectStoreConfiguration = objectStoreConfiguration;
        this.objectStoreService = objectStoreService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.channelRepository = channelRepository;
        this.videoRepository = videoRepository;
        this.tmpOriginalFileDirectory = Files.createDirectories(Path.of(StringUtils.cleanPath(tmpOriginalFileDirectory)));
        this.resultDirectory = Files.createDirectories(Path.of(StringUtils.cleanPath(resultDirectory)));
    }

//    @PostConstruct
//    public void init() throws IOException {
//        Files.createDirectories(originalFileDirectory);
//        Files.createDirectories(resultDirectory);
//    }

    @Override
    @Transactional
    public String uploadAvatar(String resource, long id, MultipartFile file) {
        Assert.isTrue(resource.equals("users") || resource.equals("channels"), "Resource must be either \"users\" or \"channel\"");

        boolean isUsersResource = resource.equals("users");
        User user = userService.getCurrentUser().orElseThrow(UnauthorizedException::new);
        String objectNamePrefix;
        Channel channel = null;

        if (isUsersResource) {
            if (id != user.getId()) throw new ForbiddenException();

            objectNamePrefix = "USER-" + user.getId();
        } else {
            channel = channelRepository.findById(id)
                    .filter((c) -> c.getUser().getId() == user.getId())
                    .orElseThrow(ForbiddenException::new);
            objectNamePrefix = STR."USER-\{user.getId()}/CHANNEL-\{channel.getId()}";
        }

        String objectName = STR."\{objectNamePrefix}/avatar\{extractFileExtensionWithDot(file.getOriginalFilename())}";
        // Stored object path's format: <avatar bucket>:USER-<user.id>[/CHANNEL-<channel.id>]/avatar.<file extension>
        String storedObjectPath = STR."\{objectStoreConfiguration.getAvatarBucketName()}:\{objectName}";

        objectStoreService.putObject(objectStoreConfiguration.getAvatarBucketName(), objectName, file);

        if (isUsersResource) {
            user.setAvatar(storedObjectPath);
            userRepository.save(user);
        } else {
            channel.setAvatar(storedObjectPath);
            channelRepository.save(channel);
        }

        return objectStoreService.getDownloadUrl(storedObjectPath);
    }

    @Override
    @Transactional
    public String uploadThumbnail(long id, MultipartFile file) {
        User user = userService.getCurrentUser().orElseThrow(UnauthorizedException::new);
        Video video = videoRepository.findById(id)
                .filter((v) -> v.getChannel().getUser().getId() == user.getId())
                .orElseThrow(ResourceNotFoundException::new);
        String fileExtensionWithDot = extractFileExtensionWithDot(file.getOriginalFilename());
        // Stored object path's format: <thumbnail bucket>:CHANNEL-<id>/<filename><file extension>
        String objectName = STR."CHANNEL-\{video.getChannel().getId()}/\{video.getCode()}\{fileExtensionWithDot}";
        String storedObjectPath = STR."\{objectStoreConfiguration.getThumbnailBucketName()}:\{objectName}";

        objectStoreService.putObject(objectStoreConfiguration.getThumbnailBucketName(), objectName, file);
        video.setThumbnail(storedObjectPath);
        videoRepository.save(video);

        return objectStoreService.getDownloadUrl(storedObjectPath);
    }

    @Override
    @Transactional
    public void uploadVideo(long id, MultipartFile file) {
//        String fileExtensionWithDot = Optional.ofNullable(file.headers().getContentType())
//                .orElseThrow(() -> new InvalidMediaTypeException("", "No \"Content-Type\" provided"))
//                .getSubtype();
        // TODO: add username + user's ID to filename
        User user = userService.getCurrentUser().orElseThrow(UnauthorizedException::new);
        Video video = videoRepository.findById(id)
                .filter((v) -> v.getChannel().getUser().getId() == user.getId())
                .orElseThrow(ResourceNotFoundException::new);

        // In case video's code is being generated
        if (video.getStatus() == Video.Status.CREATING) {
//            String taskName = String.valueOf(id);
//            ScheduledFuture<?> waitTask = videoCreationWaitScheduler.schedule(() -> {
//                var videoOptional = videoRepository.findById(id);
//
//                if (videoOptional.isPresent()) {
//                    var status = videoOptional.get().getStatus();
//
//                    if (status == Video.Status.CREATING) {
//                        log.info(STR."[ID=\{taskName}] Video is still in CREATING status.");
//                    } else if (status == Video.Status.PROCESSING || status == Video.Status.READY) {
//                        log.info(STR."[ID=\{taskName}] Video have been in \{videoOptional.get()} status. Stop wait job.");
//                        Optional.ofNullable(waitForCreatingVideoTasks.remove(taskName)).ifPresent((t) -> t.cancel(true));
//                    } else if (status == Video.Status.CREATED) {
//                        log.info(STR."[ID=\{taskName}] Video have been in \{videoOptional.get()} status. Stop wait job & start processing.");
//                        Optional.ofNullable(waitForCreatingVideoTasks.remove(taskName)).ifPresent((t) -> t.cancel(true));
//
//                    }
//                } else {
//                    log.error(STR."[ID=\{taskName}] Video does not exist. Stop periodic wait job.");
//                    Optional.ofNullable(waitForCreatingVideoTasks.remove(taskName)).ifPresent((t) -> t.cancel(true));
//                }
//            }, Instant.now().plus(3, ChronoUnit.SECONDS));

            throw new BadRequestException(ErrorCode.E00008, "Video is temporarily not available. Request again after a short time.");
        } else if (video.getStatus() == Video.Status.PROCESSING) {
            throw new BadRequestException("Being processed. Cannot re-upload.");
        }

        // Temporarily disallow user to update video content
//        if (video.getStatus() == Video.Status.READY && StringUtils.hasText(video.getDestFilepath())) {
//            throw new BadRequestException("Cannot re-upload");
//        }

//        Path filepath = Paths.get(StringUtils.cleanPath(processingDir)).resolve(UUID.randomUUID() + "-" + file.getOriginalFilename());
        String fileExtensionWithDot = extractFileExtensionWithDot(file.getOriginalFilename());
        String objectName = STR."CHANNEL-\{video.getChannel().getId()}/\{video.getCode()}\{fileExtensionWithDot}";
        // Naming format: <temp bucket>:CHANNEL-<user.id>/<filename><file extension>
        String filepath = STR."\{objectStoreConfiguration.getTemporaryBucketName()}:\{objectName}";

        objectStoreService.putObject(objectStoreConfiguration.getTemporaryBucketName(), objectName, file);
//        file.transferTo(filepath);

        video.setStatus(Video.Status.PROCESSING);
        video.setOriginalFilename(file.getOriginalFilename());
//        video.setTempFilepath(filepath.toFile().getAbsolutePath()/*.replace(":\\", "/")*/.replace("\\", "/"));
        video.setTempFilepath(filepath);

        videoRepository.save(video);
    }

    /**
     * @throws VideoProcessingException whenever an exception is thrown.
     */
//    @Transactional
    @JmsListener(destination = Constants.MessageBrokerQueueNames.VIDEO_PROCESSING_REQUEST_QUEUE, containerFactory = "videoProcessingJmsListenerContainerFactory")
    protected void processVideo(long videoId) {
        Video video = videoRepository.findById(videoId).orElseThrow(() -> new VideoProcessingException(videoId, "Video does not exist"));

        Assert.isTrue(video.getStatus() == Video.Status.PROCESSING, "Video status is not in PROCESSING status");
        Assert.notNull(video.getTempFilepath(), "Temporary filepath is null, thus the process cannot be continued.");

        LocalDateTime startTime = LocalDateTime.now();
        String[] splitTempPath = video.getTempFilepath().split(":");
        GetObjectResponse fetchedObject = objectStoreService.getObject(splitTempPath[0], splitTempPath[1]);
        log.info("Downloading file '{}' took {}", video.getTempFilepath(), Duration.between(startTime, LocalDateTime.now()).get(ChronoUnit.SECONDS));
        // includes file extension
        String originalFilename = video.getTempFilepath().substring(video.getTempFilepath().lastIndexOf("/") + 1);
        File localOriginalFile = tmpOriginalFileDirectory.resolve(originalFilename).toFile();
        Path localOutputDirPath = resultDirectory.resolve(localOriginalFile.getName());

        startTime = LocalDateTime.now();

        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(localOriginalFile), 32 * 1024)) {
            fetchedObject.transferTo(os);
        } catch (IOException e) {
            log.error("Cannot write original file '{}' to disk. Reason: {}", localOriginalFile.getAbsolutePath(), e.getMessage());
            throw new VideoProcessingException(videoId, localOriginalFile, null, e);
        }

        log.info("Saving original file as '{}' took {}", localOriginalFile.getAbsolutePath(), Duration.between(startTime, LocalDateTime.now()));

        // Number of streams/resolutions are temporarily fixed
        String[] resolutions = {"1080p", "720p", "480p"};
        MediaFileInfo mediaFileInfo = null;

        // TODO: get scale, width/height and from which compute appropriate output
        try {
            String mediaInfoJson = new String(
                    Runtime.getRuntime().exec(new String[]{
                            "ffprobe", "-v" /*loglevel*/, "warning",
                            "-show_entries", "stream=index,codec_type,coded_width,coded_height:format=duration,size,bit_rate",
                            "-of" /*output_format*/, "json", localOriginalFile.getAbsolutePath()
                    }).getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );
            mediaFileInfo = objectMapper.readValue(mediaInfoJson, MediaFileInfo.class);
//            resolutions = new String[]{"1080p", "720p", "480p"};
        } catch (IOException e) {
            log.error("Error reading file's metadata. Reason: {}", e.getMessage());
            throw new VideoProcessingException(videoId, localOriginalFile, null, e);
        }

        try {
            Files.createDirectories(resultDirectory.resolve(localOriginalFile.getName()));

            String outputPathStr = localOutputDirPath.toAbsolutePath().toString();

            log.info("localOutputDirPath: {} ; outputPathStr (cleaned) : {}", localOutputDirPath.toAbsolutePath(), outputPathStr);
//                        String ffmpegArgs = STR."""
//                             -i \{tmpOriginalFile}
//                            -c:v libx264 -c:a aac
//                            -map 0:v -map 0:a -s:v:0 640x360 -b:v:0 800k
//                            -map 0:v -map 0:a -s:v:1 1280x720 -b:v:1 2800k
//                            -map 0:v -map 0:a -s:v:2 1920x1080 -b:v:2 5000k
//                            -var_stream_map "v:0,a:0 v:1,a:0 v:2,a:0"
//                            -master_pl_name \{outputPathStr}/\{videoId}/master.m3u8
//                            -f hls -hls_time 10 -hls_list_size 0
//                            -hls_segment_filename "\{outputPathStr}/\{videoId}/v%v/fileSequence%d.ts"
//                            "\{outputPathStr}/\{videoId}/v%v/prog_index.m3u8"
//                            """;

            String[] ffmpegArgs = {
                    "ffmpeg", "-v" /*loglevel*/, "warning",
                    "-i", localOriginalFile.getAbsolutePath(),
                    "-filter_complex", "[0:v]split=3[v1][v2][v3]; [v1]scale=w=1920:h=1080[v1out]; [v2]scale=w=1280:h=720[v2out]; [v3]scale=w=854:h=480[v3out]",
                    "-map", "[v1out]", "-c:v:0", "libx264", "-b:v:0", "5000k", "-maxrate:v:0", "5350k", "-bufsize:v:0", "7500k",
                    "-map", "[v2out]", "-c:v:1", "libx264", "-b:v:1", "2800k", "-maxrate:v:1", "2996k", "-bufsize:v:1", "4200k",
                    "-map", "[v3out]", "-c:v:2", "libx264", "-b:v:2", "1400k", "-maxrate:v:2", "1498k", "-bufsize:v:2", "2100k",
                    "-map", "a:0", "-c:a", "aac", "-b:a:0", "192k", "-ac", "2",
                    "-map", "a:0", "-c:a", "aac", "-b:a:1", "128k", "-ac", "2",
                    "-map", "a:0", "-c:a", "aac", "-b:a:2", "96k", "-ac", "2",
                    "-f", "hls",
                    "-hls_time", "10",
                    "-hls_playlist_type", "vod",
                    "-hls_flags", "independent_segments",
                    "-hls_segment_type", "mpegts",
                    "-master_pl_name", MASTER_FILENAME,
                    "-var_stream_map", "v:0,a:0 v:1,a:1 v:2,a:2",
                    "-hls_segment_filename", STR."\{outputPathStr}/stream_%v/data%03d.ts",
                    STR."\{outputPathStr}/stream_%v/playlist.m3u8"
            };

            log.info("ffmpeg command: {}", String.join(" ", ffmpegArgs));
            //file this command
            ProcessBuilder processBuilder = new ProcessBuilder(ffmpegArgs
//                                Arrays.stream(ffmpegArgs.split("[ \n]+"))
//                                        .map((s) -> s.replaceAll("\\\\s", " "))
//                                        .toArray(String[]::new)
            );

            processBuilder.inheritIO();

            Process process = processBuilder.start();
            int exit = process.waitFor();

            if (exit != 0) {
//                            log.info(new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8));
                log.error("video processing failed! exit = " + exit);
                throw new VideoProcessingException(videoId, localOriginalFile, localOutputDirPath, "video processing failed! exit = " + exit);
            }
        } catch (IOException | InterruptedException e) {
            log.error("Video processing fails! Reason: {}", e.getMessage());
            throw new VideoProcessingException(videoId, localOriginalFile, localOutputDirPath, e);
        }

        List<CompletableFuture<?>> asyncIOTasks = new ArrayList<>(); // run IO tasks concurrently by creating tasks & adding to this list

        Path originalMasterFilepath = localOutputDirPath.resolve(MASTER_FILENAME);
        String outputBucket = objectStoreConfiguration.getHlsBucketName();
        String objectStoreResultFilePrefix = STR."CHANNEL-\{video.getChannel().getId()}/\{video.getCode()}";

        // With the content of master file, rename "stream_%v" with corresponding result resolution names,
        // then save the final master file into object store.
        asyncIOTasks.add(CompletableFuture.supplyAsync(() -> {
            Path destMasterFilepath = localOutputDirPath.resolve("dest-master.m3u8");

            try (
                    BufferedReader originalMasterFileReader = new BufferedReader(new FileReader(originalMasterFilepath.toFile()), 32 * 1024);
                    BufferedWriter destMasterFileWriter = new BufferedWriter(new FileWriter(destMasterFilepath.toFile()), 32 * 1024);
            ) {
                String line;

                // Rename folders in master file
                while ((line = originalMasterFileReader.readLine()) != null) {
                    for (int i = 0; i < resolutions.length; i++) {
                        line = line.replaceAll("^stream_" + i, resolutions[i]);
                    }

                    destMasterFileWriter.write(line + "\n");
                }
            } catch (IOException e) {
                try {
                    Files.delete(destMasterFilepath);
                } catch (IOException e1) {
                    log.error("Failed to delete final master file! Original cause: Failed to rename output dir in master file's content! Reason: {}", e.getMessage());
                    throw new VideoProcessingException(videoId, localOriginalFile, localOutputDirPath, e1);
                }

                log.error("Failed to rename output dir in master file's content! Reason: {}", e.getMessage());
                throw new VideoProcessingException(videoId, localOriginalFile, localOutputDirPath, e);
            }

            return destMasterFilepath;
        }).thenComposeAsync((destMasterFilepath) -> objectStoreService.putObjectAsync(
                outputBucket,
                STR."\{objectStoreResultFilePrefix}/\{MASTER_FILENAME}",
                destMasterFilepath.toFile(), "application/vnd.apple.mpegurl"
        )));

        // Save output segment files (.ts, playlist.m3u8) into object store
        for (int i = 0; i < resolutions.length; i++) {
            String resolution = resolutions[i];

            try (Stream<Path> segmentFilepathStream = Files.list(localOutputDirPath.resolve("stream_" + i))) {
                segmentFilepathStream.forEach((segmentFilepath) -> asyncIOTasks.add(
                        objectStoreService.putObjectAsync(
                                outputBucket,
                                STR."\{objectStoreResultFilePrefix}/\{resolution}/\{segmentFilepath.getFileName()}",
                                segmentFilepath.toFile(), "application/vnd.apple.mpegurl"
                        )
                ));
            } catch (IOException e) {
                throw new VideoProcessingException(videoId, localOriginalFile, localOutputDirPath, e);
            }
        }

        // If thumbnail is absent, extract a frame from the original file and save it as thumbnail instead.
        if (!StringUtils.hasText(video.getThumbnail())) {
            var mediaFileInfoFinal = mediaFileInfo;
            String fileExtension = ".png";
            String mimeType = "image/png";

            asyncIOTasks.add(CompletableFuture.supplyAsync(() -> {
                try {
                    String localThumbnailFilepath = localOutputDirPath.resolve(STR."thumbnail\{fileExtension}").toAbsolutePath().toString();

                    Process process = Runtime.getRuntime().exec(new String[]{
                            // pick up a frame at (1 / 5 * duration) of video
                            "ffmpeg", "-v" /*loglevel*/, "warning", "-ss", String.valueOf(mediaFileInfoFinal.getFormat().getDuration() * 1 / 5),
                            "-i", localOriginalFile.getAbsolutePath(), "-frames:v", "1", localThumbnailFilepath
                    });
                    int exit = process.waitFor();

                    if (exit != 0) {
                        String errorMessage = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);

                        log.error(STR."Thumbnail extracting failed! exit = \{exit}. Reason: \{errorMessage}");
                        throw new VideoProcessingException(videoId, localOriginalFile, localOutputDirPath, errorMessage);
                    }

                    return localThumbnailFilepath;
                } catch (IOException | InterruptedException e) {
                    log.error("Error extracting thumbnail. Reason: {}", e.getMessage());
                    throw new VideoProcessingException(videoId, localOriginalFile, localOutputDirPath, e);
                }
            }).thenAcceptAsync((localThumbnailFilepath) -> {
                // Stored object path's format: <thumbnail bucket>:CHANNEL-<id>/<filename><file extension>
                String objectName = STR."CHANNEL-\{video.getChannel().getId()}/\{video.getCode()}\{fileExtension}";

                objectStoreService.putObject(objectStoreConfiguration.getThumbnailBucketName(), objectName, new File(localThumbnailFilepath), mimeType);
                video.setThumbnail(STR."\{objectStoreConfiguration.getThumbnailBucketName()}:\{objectName}");
                videoRepository.save(video);
            }));
        }

        try {
            CompletableFuture.allOf(asyncIOTasks.toArray(CompletableFuture[]::new)).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error while concurrently modifying master file and saving output files into object store. Reason: {}", e.getMessage());
            throw new VideoProcessingException(videoId, localOriginalFile, localOutputDirPath, e);
        }

        asyncIOTasks.clear();

        /* Clean local storage by deleting added/created files */

        // delete local original file
        asyncIOTasks.add(CompletableFuture.runAsync(() -> {
            try {
                Files.delete(localOriginalFile.toPath());
            } catch (IOException e) {
                log.error("Error while deleting original file '{}'. Reason: {}", localOriginalFile.getAbsolutePath(), e.getMessage());
//                throw new VideoProcessingException(videoId, e);
            }
        }));

        // delete the local output directory
        asyncIOTasks.add(Helper.deleteDirectory(localOutputDirPath));

        /**/

        // Delete original file in object store
        asyncIOTasks.add(objectStoreService.removeObjectAsync(splitTempPath[0], splitTempPath[1]));

        if (StringUtils.hasText(video.getDestFilepath())) {
            String[] splitPath = video.getDestFilepath().split(":");

            asyncIOTasks.add(CompletableFuture.runAsync(() -> objectStoreService.removeObjectsWithPrefix(splitPath[0], splitPath[1])));
        }

        // Update filepath & status in database
        asyncIOTasks.add(CompletableFuture.runAsync(() -> {
            video.setStatus(Video.Status.READY);
            video.setTempFilepath(null);
            video.setDestFilepath(STR."\{outputBucket}:\{objectStoreResultFilePrefix}");

            videoRepository.save(video);
        }));

        try {
            CompletableFuture.allOf(asyncIOTasks.toArray(CompletableFuture[]::new)).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error while concurrently deleting local files and original file in object store. Reason: {}", e.getMessage());
            throw new VideoProcessingException(videoId, localOriginalFile, localOutputDirPath, e);
        }
    }

    private static String extractFileExtensionWithDot(@Nullable String fileName) {
        return Optional.ofNullable(fileName)
                .map((name) -> name.lastIndexOf("."))
                .filter((idx) -> idx > -1)
                .map((idx) -> "." + fileName.substring(idx + 1))
                .orElse("");
    }
}
