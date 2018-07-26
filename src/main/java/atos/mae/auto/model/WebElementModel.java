package atos.mae.auto.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.google.common.base.Function;

import atos.mae.auto.action.Action;
import atos.mae.auto.plugins.requirement.StepReturn;
import atos.mae.auto.plugins.requirement.StepReturnEnum;
import atos.mae.auto.plugins.requirement.WebDriverProvider;

/**
 * Class used to store name, finder, type of identifier.
 */
public class WebElementModel {

    /**
     * Logger.
     */
    private static final Logger Log = Logger.getLogger(WebElementModel.class);

    private WebDriverProvider webDriverProvider;

    protected Action action;

    /**
     * Identifier name.
     *
     */
    public String ObjectName;

    /**
     * WebElement found in page.
     */
    private WebElement webElement;

    // Identification
    /**
     * Searching by Id.
     */
    private String Id;

    /**
     * Searching by Name.
     */
    private String Name;

    /**
     * Searching by Link.
     */
    private String Link;

    /**
     * Searching by inner text.
     */
 
    private String XPathInnerText; // code change by Vivek for 55317

    /**
     * Searching by Class.
     */
    private String ClassName;

    /**
     * Searching by TagName.
     */
    private String TagName;

    /**
     * Searching by XPath.
     */
    private String XPath;


	/**
     * Searching by Css.
     */
    private String Css;

    /**
     * Web elements found list.
     */
    private List<WebElement> webElements;

    public WebElementModel() {

    }

    public WebElementModel(WebElementModel im) {
        this.ObjectName = im.ObjectName;
        this.Id = im.Id;
        this.Name = im.Name;
        this.Link = im.Link;
        this.XPathInnerText = im.XPathInnerText;
        this.ClassName = im.ClassName;
        this.TagName = im.TagName;
        this.XPath = im.XPath;
        this.Css = im.Css;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public void setWebDriverProvider(WebDriverProvider webDriverProvider) {
        this.webDriverProvider = webDriverProvider;
    }

    /**
     * Found webElement and store it from Id, XPath, Css, Link (in this order)
     * and only if only one WebElement found.
     *
     * @return Pass if WebElement found one time, else Error
     */
    final public StepReturn tryGetWebElement(int index) {
        switchtoDefaultFrame();
        this.webElement = null;

        this.webElements = new ArrayList<WebElement>();

        final String id = quoteProtect(this.Id);
        final String name = quoteProtect(this.Name);
        final String link = quoteProtect(this.Link);
        final String XPathInnerText = quoteProtect(this.XPathInnerText);
        final String classe = quoteProtect(this.ClassName);
        final String tagName = quoteProtect(this.TagName);
        final String xPath = quoteProtect(this.XPath);
        final String css = quoteProtect(this.Css);
        final WebDriver webDriverGlobal = this.webDriverProvider.getWebDriver();
        
        try {

            webElements = (List<WebElement>) this.webDriverProvider.getWait()
                    .until(new Function<WebDriver, List<WebElement>>() {
                        @Override
                        public List<WebElement> apply(WebDriver driver) {

                            // search in default content
                            Log.info("search in default content");
                            Log.info(webDriverGlobal.getCurrentUrl());
                            List<WebElement> webElements = loopFrame(id, name, link, XPathInnerText, classe, tagName,
                                    xPath, css, webDriverGlobal);
                            Log.info("end search");

                            if (webElements.size() == 0)
                            	throw new NoSuchElementException("No web element found");

                            return webElements;
                        }

                        private List<WebElement> loopFrame(String id, String name, String link, String XPathInnerText,
                                String classe, String tagName, String xPath, String css, WebDriver webDriver) {


                            List<WebElement> webElements = searchElement(webDriver, id, name, link, XPathInnerText,
                                    classe, tagName, xPath, css);

                            if (webElements.size() >= 1)
                                return webElements;

                            // search in frame
                            boolean yetAnotherFrame;
                            int index = 0;
                            do {
                                final WebDriver webDriverlocal = this.switchToFrame(webDriver, index);
                                if (webDriverlocal == null)
                                    break;
                                Log.info("search in frame n°" + index);
                                List<WebElement> newWebElements = loopFrame(id, name, link, XPathInnerText, classe, tagName, xPath, css, webDriverlocal);

                                if(newWebElements.size() == 1)
                                    return newWebElements;
                                else
                                    webElements.addAll(newWebElements);

                                index++;
                            } while (true);

                            return webElements;
                        }

                        /**
                         * Search element on page
                         * @param webDriver webDriver
                         * @param id id of element
                         * @param name name of element
                         * @param link link
                         * @param XPathInnerText XPathInnerText
                         * @param classe classe
                         * @param tagName tagName
                         * @param xPath xPath
                         * @param css css
                         * @return return a list of webElement 
                         */
                        private List<WebElement> searchElement(WebDriver webDriver, String id, String name, String link,
                                String XPathInnerText, String classe, String tagName, String xPath, String css) {
                            List<WebElement> webElements = new ArrayList<WebElement>();
                            if (id != null && !id.trim().isEmpty())
                                webElements = this.tryGetWebElementBy(webDriver, By.id(id), webElements, id, "Id");

                            if (webElements.size() != 1 && name != null && !name.trim().isEmpty())
                                webElements = this.tryGetWebElementBy(webDriver, By.name(name), webElements, name,
                                        "Name");

                            if (webElements.size() != 1 && link != null && !link.trim().isEmpty())
                                webElements = this.tryGetWebElementBy(webDriver, By.partialLinkText(link), webElements,
                                        link, "Link");

                            if (webElements.size() != 1 && XPathInnerText != null && !XPathInnerText.trim().isEmpty())
                                webElements = this.tryGetWebElementBy(webDriver, By.xpath(XPathInnerText), webElements,
                                        XPathInnerText, "XPathInnerText");

                            if (webElements.size() != 1 && classe != null && !classe.trim().isEmpty()){

                                String byClass = "";
                                if(tagName != null && !tagName.trim().isEmpty()){
                                    byClass = tagName + "[class*='" + classe + "']";
                                }else{
                                    String[] classList = classe.split(" ");
                                    for(String classItem : classList){
                                        if(classItem != null && !classItem.trim().isEmpty())
                                            byClass += "." + classItem;
                                    }
                                }
                                webElements = this.tryGetWebElementBy(webDriver, By.cssSelector(byClass), webElements,
                                        classe, "Class");
                            }

                            if (webElements.size() >= 1 && tagName != null && !tagName.trim().isEmpty())
                                webElements = this.tryGetWebElementBy(webDriver, By.tagName(tagName), webElements,
                                        tagName, "TagName");
                            // if no element found or more than one, try search
                            // by xpath and css and compare each elements found
                            // previously with element found by xpath and css
                            if (webElements.size() != 1 && xPath != null && !xPath.trim().isEmpty()) {
                                List<WebElement> webElementsTemp = this.tryGetWebElementBy(webDriver, By.xpath(xPath),
                                        webElements, xPath, "XPath");
                                if (webElementsTemp.size() == 1)
                                    webElements = webElementsTemp;
                            }
                            if (webElements.size() != 1 && css != null && !css.trim().isEmpty()) {
                                List<WebElement> webElementsTemp = this.tryGetWebElementBy(webDriver, By.cssSelector(css), webElements,
                                        css, "Css");
                                if (webElementsTemp.size() == 1)
                                    webElements = webElementsTemp;
                            }
                            return webElements;
                        }

                        private List<WebElement> tryGetWebElementBy(WebDriver webDriver, By by,
                                List<WebElement> webElements, String byString, String comparator) {
                            if (webElements.size() == 1)
                                return webElements;

                            if (webElements.size() == 0 && byString != null && !byString.trim().isEmpty()) {

                                try {
                                    webElements = webDriver.findElements(by);


                                } catch (Exception e) {}

                                Log.info(webElements.size() + " WebElement(s) found with '" + by.toString() + "'.");

                            } else {
                                final List<WebElement> webElems = new ArrayList<WebElement>();
                                webElems.addAll(webElements);
                                if (byString != null && !byString.trim().isEmpty()) {
                                    for (final WebElement webElement : webElements) {

                                        if (comparator.compareTo("XPath") == 0 || comparator.compareTo("Css") == 0) {
                                            WebElement we = null;
                                            try{
                                                we = webDriver.findElement(by);
                                            } catch (NoSuchElementException e) {
                                                continue;
                                            }
                                            if (we.equals(webElement)) {
                                                webElems.clear();
                                                webElems.add(we);
                                                return webElems;
                                            }

                                        } else {
                                            final String compare = this.compareWith(webElement, comparator);
                                            if (compare.compareTo("") != 0
                                            && compare.toUpperCase().compareTo(byString.toUpperCase()) != 0) {
                                                webElems.remove(webElement);
                                            }
                                        }
                                    }
                                    if (webElems.size() == 1)
                                        return webElems;
                                    if (webElems.size() > 0 && webElems.size() != webElements.size()) {
                                        webElements.clear();
                                        webElements.addAll(webElems);
                                    }
                                }
                            }
                            return webElements;
                        }

                        public WebDriver switchToFrame(WebDriver webDriver, int frame) {
                            try {
                                return webDriver.switchTo().frame(frame);
                            } catch (NoSuchFrameException e) {
                                return null;
                            } catch (Exception e) {
                                return null;
                            }
                        }





                        private String compareWith(WebElement webElement, String comparator) {
                            switch (comparator) {
                            case "Name":
                                return webElement.getAttribute("name");
                            case "Link":
                                return webElement.getAttribute("href");
                            case "Class":
                                return webElement.getAttribute("class");
                            case "TagName":
                                return webElement.getTagName();
                            }
                            return "";

                        }
                    });
        } catch (TimeoutException | NoSuchElementException e) {
        	Log.warn(e.getMessage());
        }

        Iterator<WebElement> it = this.webElements.iterator();
        while (it.hasNext()) {

            WebElement we = it.next();
            try{
                if(!we.isDisplayed())
                    it.remove();
            }catch(StaleElementReferenceException e){
                it.remove();
            }
        }

        if (this.webElements.size() == 1) {
            this.webElement = this.webElements.get(0);
            return new StepReturn(StepReturnEnum.PASS);
        } else if (this.webElements.size() == 0) {
            return new StepReturn(StepReturnEnum.ERROR,
                    "Object '" + this.ObjectName + "' doesn't have available finder.");
        } else {
            if(this.webElements.size() <= index )
                index = 0;
            this.webElement = this.webElements.get(index);
            return new StepReturn(StepReturnEnum.WARN, "Object '" + this.ObjectName + "' found more than 1 time ( " +  webElements.size() + " times).");
        }

    }

    public void switchtoDefaultFrame() {
        try {
            webDriverProvider.getWebDriver().switchTo().defaultContent();
        } catch (Exception e) {
        	Log.warn(e.getMessage());
        }
    }

    private String quoteProtect(String value) {
        if (value == null || value.isEmpty())
            return value;

        // String valueReturn = value.replace("\'","\\'");
        String valueReturn = value;
        return valueReturn;

    }

    public String getObjectName() {
        return ObjectName;
    }

    public void setObjectName(String objectName) {
        ObjectName = objectName;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getLink() {
        return Link;
    }

    public void setLink(String link) {
        Link = link;
    }

    public String getClassAttr() {
        return ClassName;
    }

    public void setClassAttr(String class1) {
        ClassName = class1;
    }

    public String getTagName() {
        return TagName;
    }

    public void setTagName(String tagName) {
        TagName = tagName;
    }

    public String getXPath() {
        return XPath;
    }

    public void setXPath(String xPath) {
        XPath = xPath;
    }
    
 // code change by Vivek for 55317
    public String getXPathInnerText() {
		return XPathInnerText;
	}

	public void setXPathInnerText(String xPathInnerText) {
		XPathInnerText = xPathInnerText;
	}
	// end of code change by Vivek for 55317

    public String getCss() {
        return Css;
    }

    public void setCss(String css) {
        Css = css;
    }

    public WebElement getWebElement() {
        return this.webElement;
    }

    public void setWebElement(WebElement webElement) {
        this.webElement = webElement;
    }

    public List<WebElement> getWebElements() {
        return webElements;
    }

    public void setWebElements(List<WebElement> webElements) {
        this.webElements = webElements;
    }



}
