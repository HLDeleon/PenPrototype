package com.android.samplesmartpen.parsers;

import com.android.samplesmartpen.models.Content;
import com.android.samplesmartpen.models.Option;
import com.android.samplesmartpen.models.Question;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.regex.Pattern;

public class SetHandler extends DefaultHandler {

    private Content content;
    private Question question;
    private Option option;
    private String fsString = "";

    public interface OnSetParsed {
        void onSetParsed(Content content);
    }

    private OnSetParsed onSetParsed;

    public void setOnSetParsed(OnSetParsed onSetParsed) {
        this.onSetParsed = onSetParsed;
    }

    public SetHandler() {
        super();

    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        this.fsString = "";

        if (localName.equalsIgnoreCase("CONTENT")) {
            content = new Content();
            content.setID(Integer.parseInt(atts.getValue("ID")));
            content.setPageRange(atts.getValue("PAGERANGE"));
        }
        if (localName.equalsIgnoreCase("QUESTION")) {
            question = new Question();
            question.setID(Integer.parseInt(atts.getValue("ID")));
            question.setQNo(Integer.parseInt(atts.getValue("QNO")));
            question.setQType(Integer.parseInt(atts.getValue("QTYPE")));
            question.setQnoCoordinate(parseCoordinates(atts, "QCOOR")[0], parseCoordinates(atts, "QCOOR")[1],
                    parseCoordinates(atts, "QCOOR")[2], parseCoordinates(atts, "QCOOR")[3]);
        }

        if (localName.equalsIgnoreCase("MEDIA")) {
            question.setMediaCoordinate(parseCoordinates(atts, "MEDIACOOR")[0], parseCoordinates(atts, "MEDIACOOR")[1],
                    parseCoordinates(atts, "MEDIACOOR")[2], parseCoordinates(atts, "MEDIACOOR")[3]);
        }
        if (localName.equalsIgnoreCase("OPTION")) {
            option = new Option();
            option.setOptionCoordinate(parseCoordinates(atts, "OPTIONCOOR")[0], parseCoordinates(atts, "OPTIONCOOR")[1],
                    parseCoordinates(atts, "OPTIONCOOR")[2], parseCoordinates(atts, "OPTIONCOOR")[3]);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        super.characters(ch, start, length);
        this.fsString = this.fsString + new String(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (localName.equalsIgnoreCase("CONTENT")) {
            onSetParsed.onSetParsed(content);
        }
        if (localName.equalsIgnoreCase("DIRECTION")) {
            content.setDirections(fsString.trim());
        }
        if (localName.equalsIgnoreCase("MEDIA")) {
            question.setMedia(fsString.trim());
        }
        if (localName.equalsIgnoreCase("QUEST")) {
            question.setQuest(fsString.trim());
        }
        if (localName.equalsIgnoreCase("ANSWER")) {
            question.setAnswer(fsString.trim());
        }
        if (localName.equalsIgnoreCase("OPTION")) {
            option.setOption(fsString.trim());
            question.addOptions(option);
        }
        if (localName.equalsIgnoreCase("QUESTION")) {
            content.addQuestion(question);
        }

    }

    private float[] parseCoordinates(Attributes atts, String value) {
        String stringCoordinate = atts.getValue(value);

        if (stringCoordinate.equals("0")) {
            return new float[]{0, 0, 0, 0};
        }

        String[] results = stringCoordinate.split(Pattern.quote("|"));
        float[] fresults = new float[results.length];
        for (int i = 0; i < results.length; i++) {
            if (results[i].isEmpty()) {
                fresults[i] = 0;
            } else
                fresults[i] = Float.parseFloat(results[i].trim());
        }
        return fresults;
    }
}
