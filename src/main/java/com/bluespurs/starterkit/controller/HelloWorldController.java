package com.bluespurs.starterkit.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import org.xml.sax.InputSource;


@RestController
public class HelloWorldController {
    String getPriceAndNameFromXml(String xmlString, String nameTag, String productName[], float productPrice[]){
        try{
            Document xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(new InputSource(new StringReader(xmlString)));//Create Xml document from Xml string

            xmlDoc.getDocumentElement().normalize();

            NodeList errNodes = xmlDoc.getElementsByTagName("error");
            if (errNodes.getLength() > 0) {//Make sure nodes are correctly created
                Element err   = (Element)errNodes.item(0);
                return "Error while creating nodeList";
            } else { 
                //Because items are already sorted by price, just get the first item from the list
                //and use the item price in order to compare both stores.
                NodeList list = xmlDoc.getElementsByTagName(nameTag);//Here the "product" name tag may differ for the walmart API
                                                                               //Because of the error message I cannot verify;
                Node node = list.item(0);
                Element element = (Element) node;
                productPrice[0] = Float.parseFloat(element.getElementsByTagName("salePrice").item(0).getTextContent());//Get Store price
                productName [0] = element.getElementsByTagName("name").item(0).getTextContent();//Get product name
            }
        }
        catch (Exception ex) {
            return "Error !\n" +Arrays.toString(ex.getStackTrace());
        }
        return "";
    };

    String createXmlString(String apiUrl){
        try {
            String xmlString = "";
            //The Java.net.URL handle tcp/ip connection and errors are catched below
            URL url = new URL(apiUrl);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String strTemp = "";
            while (null != (strTemp = reader.readLine())) {
                xmlString += strTemp;
            }
            return xmlString;
        }
            catch (Exception ex) {
            return "Error !\n" +Arrays.toString(ex.getStackTrace());
        }
    };

    @RequestMapping("/product/search")
    public String searchProduct(@RequestParam(value="name") String name) {
            String bestBuyXml = "";//
            String walmartXml = "";
            String bestBuyStr = "";
            String walmartStr = "";
            String errors     = "";
            String[] bestBuyProductName = new String[1];
            String[] walmartProductName = new String[1];
            float[]    bestBuyPrice     = new float[1];
            float[]    walmartPrice     = new float[1];;
        try {

            bestBuyXml = createXmlString("http://api.bestbuy.com/v1/products((search="+name+"))?show=name,salePrice&sort=salePrice&format=xml&apiKey=pfe9fpy68yg28hvvma49sc89");

            errors = getPriceAndNameFromXml(bestBuyXml, "product", bestBuyProductName, bestBuyPrice);

            if(errors != ""){//Handling Xml errors
                return errors;
            }

            walmartXml = createXmlString("http://api.walmartlabs.com/v1/search?apiKey={rm25tyum3p9jm9x9x7zxshfa}&sort=price&query="+name);

            errors = getPriceAndNameFromXml(walmartXml, "product", walmartProductName, walmartPrice);

            if(errors != ""){//Handling Xml errors
                return errors;
            }


            //Product Comparison
            if(bestBuyPrice[0] < walmartPrice[0]){
                        return          "200 OK\n\n"+
                                        "{\n\t\"productName\": \""+bestBuyProductName[0]+"\","+
                                         "\n\t\"bestPrice\": \""+bestBuyPrice[0]+"\","+
                                         "\n\t\"currency\": \"CAD\","+
                                         "\n\t\"location\": \"Best Buy\"\n}";
            }
            else if(bestBuyPrice[0] > walmartPrice[0]){
                    return "200 OK\n\n"+
                            "{\n\t\"productName\": \""+walmartProductName[0]+"\","+
                             "\n\t\"bestPrice\": \""+walmartPrice[0]+"\","+
                             "\n\t\"currency\": \"CAD\","+
                             "\n\t\"location\": \"WalMart\"\n}";
            }
            else if(bestBuyPrice[0] == walmartPrice[0]){
                    bestBuyStr =  "200 OK\n\n"+
                                        "{\n\t\"productName\": \""+bestBuyProductName[0]+"\","+
                                         "\n\t\"bestPrice\": \""+bestBuyPrice[0]+"\","+
                                         "\n\t\"currency\": \"CAD\","+
                                         "\n\t\"location\": \"Best Buy and WalMart\"\n}";
            }


        } 
        catch (Exception ex) {
            return "Error !\n" +Arrays.toString(ex.getStackTrace());
        }

        //return bestBuyStr;
    }
}
