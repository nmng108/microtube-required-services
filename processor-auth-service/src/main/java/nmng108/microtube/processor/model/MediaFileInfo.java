package nmng108.microtube.processor.model;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;

import java.io.IOException;
import java.util.List;

/**
 * Cast media file's information provided by ffmpeg into this object.
 */
@Getter
public class MediaFileInfo {
    private List<Stream> streams;
    private Format format; // container's info

    public List<Stream> getVideoStream() {
        return streams.stream().filter((s) -> s.codecType == CodecType.VIDEO).toList();
    }

    public List<Stream> getAudioStream() {
        return streams.stream().filter((s) -> s.codecType == CodecType.AUDIO).toList();
    }

    @Getter
    public static class Stream {
        private int index; // from 0
        @JsonDeserialize(using = CodecTypeEnumJsonDeserializer.class)
        private CodecType codecType;
        private Integer codedWidth;
        private Integer codedHeight;
    }

    @Getter
    public static class Format {
        private Double duration; // seconds
        private Long size; // bytes
        private Long bitRate; // bits/second
    }

    public enum CodecType {
        VIDEO("video"),
        AUDIO("audio");

        public final String name;

        CodecType(String name) {
            this.name = name;
        }

//        public static CodecType valueOf(String name) {
//            for (CodecType value : values()) {
//                if (value.name.equals(name)) {
//                    return value;
//                }
//            }
//
//            return null;
//        }
    }

    public static final class CodecTypeEnumJsonDeserializer extends JsonDeserializer<CodecType> {
        @Override
        public CodecType deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
            JsonToken currentToken = jsonParser.getCurrentToken();

            return (currentToken == JsonToken.VALUE_STRING)
                    ? CodecType.valueOf(jsonParser.getText())
                    : null;
        }
    }
}
