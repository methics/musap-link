package fi.methics.webapp.musaplink.util;

import java.io.IOException;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;

import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.bind.util.ISO8601Utils;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * Marshal XML dateTime format ISO8601 date+time to/from java.util.Date
 */
public class DateMarshaller extends TypeAdapter<Date> {

    @Override
    public Date read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        final String s = reader.nextString();
        try {
            return ISO8601Utils.parse(s, new ParsePosition(0));
        } catch (ParseException e) {
            throw new JsonSyntaxException(s, e);
        }
    }
    @SuppressWarnings("resource")
    @Override
    public void write(JsonWriter writer, Date value) throws IOException {
        if (value == null) {
            writer.nullValue();
        } else {
            // Output the value with milliseconds, on UTC time zone
            writer.value( ISO8601Utils.format(value, true) );
        }
    }
}
