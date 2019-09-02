package com.android.samplesmartpen.parsers;


import com.android.samplesmartpen.models.Category;
import com.android.samplesmartpen.models.Level;
import com.android.samplesmartpen.models.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

/**
 * Created by HL on 5/31/2018.
 */

public class CategoryHandler extends DefaultHandler {
    private Category category;
    private Level level;
    private Set set;

    private boolean onCategoryTag;
    private boolean onLevelTag;
    private boolean onSetTag;

    public interface OnCategoryParsed {
        void onCategoryParsed(Category category);
    }

    private CategoryHandler.OnCategoryParsed onCategoryParsed;

    public void setOnCategoryParsed(CategoryHandler.OnCategoryParsed onCategoryParsed) {
        this.onCategoryParsed = onCategoryParsed;
    }

    public CategoryHandler() {
        super();

    }

    @Override
    public void startElement(String namespacesURI, String localName,
                             String qName, Attributes atts) throws SAXException {
        if (localName.equalsIgnoreCase("CATEGORY")) {
            if (!onCategoryTag) {
                this.category = new Category();
                this.category.setFiId(Integer.parseInt(atts.getValue("ID")));
                this.category.setFiPId(Integer.parseInt(atts.getValue("PID")));
                this.category.setFsName(atts.getValue("Name"));
                this.category.setFsCode(atts.getValue("Code"));
                this.category.setFiOrder(Integer.parseInt(atts.getValue("Order")));
                this.category.addCategory(category);
                this.onCategoryTag = true;
            } else if (!onLevelTag) {
                this.level = new Level();

                this.level.setFiId(Integer.parseInt(atts.getValue("ID")));
                this.level.setFiPId(Integer.parseInt(atts.getValue("PID")));
                this.level.setFsName(atts.getValue("Name"));
                this.level.setFsCode(atts.getValue("Code"));
                this.level.setFiOrder(Integer.parseInt(atts.getValue("Order")));
                this.category.addLevel(this.level);

                this.onLevelTag = true;
            } else {
                this.set = new Set();

                this.set.setFiId(Integer.parseInt(atts.getValue("ID")));
                this.set.setFiPId(Integer.parseInt(atts.getValue("PID")));
                this.set.setFsName(atts.getValue("Name"));
                this.set.setFsCode(atts.getValue("Code"));
                this.set.setFiOrder(Integer.parseInt(atts.getValue("Order")));

                // this.category.levels.get(levelCounter).addSet(set);

                this.level.addSet(set);
                this.onSetTag = true;
            }
        }
    }

    @Override
    public void characters(char ch[], int start, int length) {

    }

    @Override
    public void endElement(String namespaceURL, String localName, String qName)
            throws SAXException {
        if (localName.equalsIgnoreCase("CATEGORY")) {

            if (onSetTag) {
                this.onSetTag = false;
            } else if (onLevelTag) {
                this.onLevelTag = false;
            } else {
                this.onCategoryTag = false;
                this.onCategoryParsed.onCategoryParsed(category);
            }
        }
    }
}
