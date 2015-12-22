package com.example.stevenpila.loadcentraldiary;

import android.content.Context;
import android.os.Environment;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Steven Pila on 11/18/2015.
 */
public class PDFFileParser {
    private final String m_file_name;
    private String m_error_message = "";

    private final String REGEX_NEW_LINE = "[\\r\\n]+";
    private final String REGEX_WHITESPACE = "\\s+";
    private final String REGEX_IS_VALID_LINE = "[0-9]*\\.?[0-9]+%";  // E.g., 1.5%, 1%, 30%...
    private final String REGEX_GET_VALID_LINE = "([A-z0-9]+|[A-z]+<amount>)(\\s)([0-9]*\\.?[0-9]+%)"; // E.g, SMGT10 0.70%, GMXMAX<amount> 2.75%...
    private final String REGEX_GET_PRODUCT_NAME = "^(.*?)([^\\*\\(])*"; // E.g., E-load, Globe...
    private final String REGEX_IS_VALID_PRODUCT_NAME = "(New|Denoms|Denominations)+";   // E.g., New, Denoms, Denominations...
    private final String REGEX_IS_WITH_NEW = "^(.*)(New!)$";
    private final String REGEX_GET_PRODUCT_DESCRIPTION = "^(.*?)(?=" + REGEX_GET_VALID_LINE + ")";

                    // category     // name             // code
    private LinkedHashMap<String, LinkedHashMap<String, ArrayList<ProductLoadInfo>>> m_product_info_list;

    private final String DEFAULT_PDF_FILE_NAME = "11-15.pdf";

    // for testing...
    public String m_response = "";

    public PDFFileParser() {
        m_file_name = DEFAULT_PDF_FILE_NAME;
        m_product_info_list = new LinkedHashMap<>();
    }
    public PDFFileParser(String file_name) {
        m_file_name = file_name;
        m_product_info_list = new LinkedHashMap<>();
    }

    public boolean loadProductLoadList(Context context) {
        final ArrayList<String> PRODUCT_CATEGORY_LIST =  new ArrayList<>(Arrays.asList(context.getResources().getStringArray(R.array.product_category)));
        m_error_message = "";
        boolean bRet;

        try {
//            StringBuilder output = new StringBuilder();
            String filePath = null;

            if(m_file_name.equals(DEFAULT_PDF_FILE_NAME)) {
                if((bRet = MyUtility.copyAssetToData(context, m_file_name, m_file_name, MyUtility.DirectoryType.FILES)))
                    filePath = context.getFilesDir().getAbsolutePath() + File.separator + m_file_name;
                else
                    m_error_message += "PDFFileParser::loadProductLoadList - Failed to copy file (" + m_file_name + ") from assets to data.\n";
            }
            else {
                filePath = Environment.getExternalStorageDirectory() + "/" + m_file_name;   // @temp change to actual filepath
                if((bRet = MyUtility.isFileExists(filePath)))
                    m_error_message += "PDFFileParser::loadProductLoadList - File (" + filePath + ") does not exists.\n";
            }

            if(bRet) {
                PdfReader pdfReader = new PdfReader(new FileInputStream(filePath));
                PdfReaderContentParser parser = new PdfReaderContentParser(pdfReader);

                StringWriter stringWriter = new StringWriter();

                TextExtractionStrategy textExtractionStrategy;
                for (int i = 1; i <= pdfReader.getNumberOfPages(); i++) {
                    textExtractionStrategy = parser.processContent(i, new SimpleTextExtractionStrategy());

                    stringWriter.write(textExtractionStrategy.getResultantText());
                }

                String[] strLines = stringWriter.toString().split(REGEX_NEW_LINE);
                String currentProductCategory = "";
                String currentProductName = "";
                boolean isHeaderRemoved = false;

                for (String line : strLines) {   // each line in PDF file
                    if (isLineValid(line.trim())) {  // product code w/ discount line
                        String strLine = getValidLine(line.trim());
                        if (!strLine.isEmpty()) {
                            String[] splitLine = strLine.split(REGEX_WHITESPACE);
                            if (splitLine.length > 0 && splitLine.length % 2 == 0) {
                                for (int length = splitLine.length, index = 0; length > 1; length /= 2) {
                                    if (!currentProductCategory.isEmpty() && !currentProductName.isEmpty()) {
                                        String productDescription = getProductDescription(line.trim());
                                        ProductLoadInfo productLoadInfo = new ProductLoadInfo(splitLine[index++].trim(), productDescription, splitLine[index++].trim());
                                        //                                    m_response += "\t\tProduct: " + productLoadInfo.m_product + " Discount: " + productLoadInfo.m_discount + "\n";
                                        if (m_product_info_list.get(currentProductCategory) != null && m_product_info_list.get(currentProductCategory).get(currentProductName) != null)
                                            m_product_info_list.get(currentProductCategory).get(currentProductName).add(productLoadInfo);
                                    }
                                }
                            }
                        }
                    } else {  // product category/name line
                        Pattern pattern = Pattern.compile(REGEX_GET_PRODUCT_NAME);
                        Matcher matcher = pattern.matcher(line.trim());

                        if (matcher.find()) {
                            String strProduct = matcher.group().trim();

                            if (strProduct.matches(REGEX_IS_WITH_NEW)) {  // removes unexpected string (E.g, New!)
                                strProduct = strProduct.substring(0, strProduct.indexOf("New!")).trim();
                            }

                            if (!strProduct.isEmpty()) {
                                if (isValidProductCategoryName(strProduct)) {    // check if string doesn't have New|Denoms|Denominations string
                                    if (PRODUCT_CATEGORY_LIST.contains(strProduct)) { // product category
                                        currentProductCategory = strProduct;
                                        //                                    m_response += currentProductCategory + "\n";
                                        if (m_product_info_list.get(currentProductCategory) == null)
                                            m_product_info_list.put(currentProductCategory, new LinkedHashMap<String, ArrayList<ProductLoadInfo>>());
                                    } else {   // product name
                                        if (!isHeaderRemoved) {  // skip header row
                                            isHeaderRemoved = true;
                                            continue;
                                        }

                                        if (currentProductCategory.equals("E-load") && (strProduct.equals("Globe") || strProduct.equals("Touch Mobile")))
                                            currentProductName = "Globe & Touch Mobile";
                                        else
                                            currentProductName = strProduct;
                                        //                                    m_response += "\t" + currentProductName + "\n";
                                        if (m_product_info_list.get(currentProductCategory) != null && m_product_info_list.get(currentProductCategory).get(currentProductName) == null)
                                            m_product_info_list.get(currentProductCategory).put(currentProductName, new ArrayList<ProductLoadInfo>());
                                    }
                                }
                            }
                        }
                    }
                }

                pdfReader.close();
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            m_error_message += "PDFFileParser::loadProductLoadList - ArrayIndexOutOfBoundsException: " + e.getMessage() + "\n";
            bRet = false;
        } catch (NullPointerException e) {
            m_error_message += "PDFFileParser::loadProductLoadList - NullPointerException: " + e.getMessage() + "\n";
            bRet = false;
        } catch (IOException e) {
            m_error_message += "PDFFileParser::loadProductLoadList - IOException: " + e.getMessage() + "\n";
            bRet = false;
        } catch (Exception e) {
            m_error_message += "PDFFileParser::loadProductLoadList - Exception: " + e.getMessage() + "\n";
            bRet = false;
        }

        if(!bRet)
            m_product_info_list.clear();

        return bRet;
    }

    private boolean isLineValid(String line) {
        Pattern pattern = Pattern.compile(REGEX_IS_VALID_LINE);
        Matcher matcher = pattern.matcher(line);

        return matcher.find();
    }

    private String getValidLine(String line) {
        String validLine = "";

        Pattern pattern = Pattern.compile(REGEX_GET_VALID_LINE);
        Matcher matcher = pattern.matcher(line);

        while(matcher.find())
            validLine += matcher.group().trim() + " ";

        return validLine.trim();
    }

    private String getProductDescription(String line) {
        String productDescription = "";

        Pattern pattern = Pattern.compile(REGEX_GET_PRODUCT_DESCRIPTION);
        Matcher matcher = pattern.matcher(line.trim());

        if(matcher.find())
            productDescription = matcher.group().trim();

        return productDescription;
    }

    private boolean isValidProductCategoryName(String productCategoryName) {
        Pattern pattern = Pattern.compile(REGEX_IS_VALID_PRODUCT_NAME);
        Matcher matcher = pattern.matcher(productCategoryName);

        return !matcher.find();
    }

    public LinkedHashMap<String, LinkedHashMap<String, ArrayList<ProductLoadInfo>>> getProductInfoList() { return m_product_info_list; }
    public boolean isProductInfoListEmpty() { return m_product_info_list.isEmpty(); }
    public String getErrorMessage() {
        return m_error_message;
    }

    // for testing...
//    public String getProductList() {
//        String strProduct = "";
//
//        for (Map.Entry<String, LinkedHashMap<String, ArrayList<ProductLoadInfo>>> product_name_list : m_product_info_list.entrySet()) {
//            strProduct += product_name_list.getKey() + "\n";
//            for(Map.Entry<String, ArrayList<ProductLoadInfo>> product_code : product_name_list.getValue().entrySet()) {
//                strProduct += "\t" + product_code.getKey() + "\n";
//                for(ProductLoadInfo productLoadInfo : product_code.getValue()) {
//                    strProduct += "\t\t Product: " + productLoadInfo.m_product + " Discount: " + productLoadInfo.m_discount + "\n";
//                }
//            }
//        }
//
//        return strProduct;
//    }
}
