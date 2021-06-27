import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

class Main {
    public static final String FILE_NAME_CSV = "data.csv";
    public static final String FILE_NAME_XML = "data.xml";
    public static final String FILE_NAME_DATA = "data.json";
    public static final String FILE_NAME_DATA2 = "data2.json";
    public static final String[] COLUMN_MAPPING = {"id", "firstName", "lastName", "country", "age"};

    public static void main(String[] args) {
        List<Employee> listCsv = parseCSV(COLUMN_MAPPING, FILE_NAME_CSV);
        List<Employee> listXml = parseXML(COLUMN_MAPPING, FILE_NAME_XML);
        String jsonCsv = listToJson(listCsv);
        writeString(jsonCsv, FILE_NAME_DATA);
        String jsonXml = listToJson(listXml);
        writeString(jsonXml, FILE_NAME_DATA2);
        String json = readString(FILE_NAME_DATA);
        List<Employee> list = jsonToList(json);
        for (Employee employee : list) {
            System.out.println(employee.toString());
        }
    }

    public static List<Employee> parseCSV(String[] columnMapping, String fileName) {
        List<Employee> list = null;
        try (CSVReader csvReader = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);
            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(csvReader)
                    .withMappingStrategy(strategy)
                    .build();
            list = csv.parse();
            //System.out.println(list);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static String listToJson(List<Employee> list) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Type listType = new TypeToken<List<Employee>>() {
        }.getType();
        return gson.toJson(list, listType);
    }

    public static void writeString(String json, String fileName) {
        try (FileWriter file = new FileWriter(fileName)) {
            file.write(json);
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Employee> parseXML(String[] COLUMN_MAPPING, String fileName) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        List<Employee> list = new ArrayList<>();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(fileName));
            Node root = doc.getDocumentElement();
            NodeList nodeList = root.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (Node.ELEMENT_NODE == node.getNodeType()) {
                    Element element = (Element) node;
                    String idString = getElement(element, COLUMN_MAPPING[0]);
                    long id = Long.parseLong(idString);
                    String firstName = getElement(element, COLUMN_MAPPING[1]);
                    String lastName = getElement(element, COLUMN_MAPPING[2]);
                    String country = getElement(element, COLUMN_MAPPING[3]);
                    String ageString = getElement(element, COLUMN_MAPPING[4]);
                    int age = Integer.parseInt(ageString);
                    Employee employee = new Employee(id, firstName, lastName, country, age);
                    list.add(employee);
                }
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        //System.out.println(list);
        return list;
    }

    public static String getElement(Element element, String name) {
        return element.getElementsByTagName(name).item(0).getTextContent();
    }

    public static String readString(String FILE_NAME_DATA) {
        String strJson = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME_DATA))) {
            while ((strJson = reader.readLine()) != null) {
                return strJson;
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public static List<Employee> jsonToList(String json) {
        List<Employee> list = new ArrayList<>();
        JSONParser parser = new JSONParser();
        try {
            JSONArray employee = (JSONArray) parser.parse(json);
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            for (Object emp : employee) {
                list.add(gson.fromJson(emp.toString(), Employee.class));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return list;
    }
}
