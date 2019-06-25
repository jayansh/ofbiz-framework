/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ofbiz.webtools;

import org.apache.ofbiz.base.util.Base64;
import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilIO;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.GenericEntity;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.model.ModelField;

import java.io.PrintWriter;
import java.text.StringCharacterIterator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class EntityJsonHelper {

    public static final String module = EntityJsonHelper.class.getName();

    /**
     * Writes JSON text for each field of the entity
     * @param writer A PrintWriter to write to
     */
    public static void writeJsonText(PrintWriter writer, GenericValue value) {
        Map<String, String> fieldMap = new HashMap<>();

        Iterator<ModelField> modelFields = value.getModelEntity().getFieldsIterator();
        while (modelFields.hasNext()) {
            ModelField modelField = modelFields.next();
            String name = modelField.getName();

            String type = modelField.getType();
            if (type != null && "blob".equals(type)) {
                Object obj = value.get(name);
                boolean b1 = obj instanceof byte[];
                if (b1) {
                    byte[] binData = (byte[]) obj;
                    String strData = new String(Base64.base64Encode(binData), UtilIO.getUtf8());
                    if (UtilValidate.isNotEmpty(strData)) {
                        fieldMap.put(name, strData);
                    }
                } else {
                    Debug.logWarning("Field:" + name + " is not of type 'byte[]'. obj: " + obj, module);
                }
            } else {
                String valueStr = value.getString(name);
                if (UtilValidate.isNotEmpty(valueStr)) {
                    // check each character, if line-feed or carriage-return is found set needsCdata to true; also look for invalid characters
                    /*for (int i = 0; i < valueStrBld.length(); i++) {
                        char curChar = valueStrBld.charAt(i);

                        switch (curChar) {
                            case '\\':
                                valueStrBld.replace(i, i + 1, "\\\\");
                                break;
                            case '/':
                                valueStrBld.replace(i, i + 1, "\\/");
                                break;
                            case 0x8: //backspace, \b
                                valueStrBld.replace(i, i + 1, "\\b");
                                break;
                            case 0xC: // form feed, \f
                                valueStrBld.replace(i, i + 1, "\\f");
                                break;
                            case 0xA: // newline, \n
                                valueStrBld.replace(i, i + 1, "\\n");
                                break;
                            case 0xD: // carriage return, \r
                                valueStrBld.replace(i, i + 1, "\\r");
                                break;
                            case 0x9:// tab, \t
                                valueStrBld.replace(i, i + 1, "\\t");
                                break;
                            case '"':
                                valueStrBld.replace(i, i + 1, "\"");
                                break;
                        }

                }*/
                    valueStr = normalizeJSON(valueStr);
                    fieldMap.put(name, valueStr);
                }
            }
        }
        writer.println('{');
        if (fieldMap.size() != 0) {
            int index = 0;
            for (Map.Entry<String, String> fieldEntry : fieldMap.entrySet()) {
                ++index;
                writer.print("\"");
                writer.print(fieldEntry.getKey());
                writer.print("\"");
                writer.print(":");
                writer.print("\"");
                writer.print(fieldEntry.getValue());
                writer.print("\"");
                if (index < fieldMap.size()) {
                    writer.print(",");
                }
            }
        }
        writer.print("}");
    }

    /**
     * Writes JSON text for each field of the entity
     * @param textBuilder A StringBuilder to write to entity to be writter
     */
    public static void writeJsonText(StringBuilder textBuilder, GenericValue value) {
        Map<String, String> fieldMap = new HashMap<>();

        Iterator<ModelField> modelFields = value.getModelEntity().getFieldsIterator();
        while (modelFields.hasNext()) {
            ModelField modelField = modelFields.next();
            String name = modelField.getName();

            String type = modelField.getType();
            if (type != null && "blob".equals(type)) {
                Object obj = value.get(name);
                boolean b1 = obj instanceof byte[];
                if (b1) {
                    byte[] binData = (byte[]) obj;
                    String strData = new String(Base64.base64Encode(binData), UtilIO.getUtf8());
                    if (UtilValidate.isNotEmpty(strData)) {
                        fieldMap.put(name, strData);
                    }
                } else {
                    Debug.logWarning("Field:" + name + " is not of type 'byte[]'. obj: " + obj, module);
                }
            } else {
                String valueStr = value.getString(name);
                if (UtilValidate.isNotEmpty(valueStr)) {
                    valueStr = normalizeJSON(valueStr);
                    fieldMap.put(name, valueStr);
                }
            }
        }
        textBuilder.append('{');
        if (fieldMap.size() != 0) {
            int index = 0;
            for (Map.Entry<String, String> fieldEntry : fieldMap.entrySet()) {
                ++index;
                textBuilder.append("\"");
                textBuilder.append(fieldEntry.getKey());
                textBuilder.append("\"");
                textBuilder.append(":");
                textBuilder.append("\"");
                textBuilder.append(fieldEntry.getValue());
                textBuilder.append("\"");
                if (index < fieldMap.size()) {
                    textBuilder.append(",");
                }
            }
        }
        textBuilder.append("}");
    }

    public static String normalizeJSON(String aText) {
        if (UtilValidate.isEmpty(aText)) {
            return aText;
        }
        //StringBuilder result = new StringBuilder();
        String result = new String();
        StringCharacterIterator iterator = new StringCharacterIterator(aText);
        char character = iterator.current();
        while (character != StringCharacterIterator.DONE) {
            /*switch (character) {
                case '\\':
                case '/':
                case 0x8: //backspace, \b
                case 0xC: // form feed, \f
                case 0xA: // newline, \n
                case 0xD: // carriage return, \r
                case 0x9:// tab, \t
                case '"':
                    result.append("\\");
                default:
                    result.append(character);
                    character = iterator.next();
                    break;
            }*/
            switch (character) {
                case '\\':
                    result = result + "\\\\";
                    character = iterator.next();
                    break;
                case '/':
                    result = result + "\\/";
                    character = iterator.next();
                    break;
                case 0x8: //backspace, \b
                    result = result + "\\\\b";
                    character = iterator.next();
                    break;
                case 0xC: // form feed, \f
                    result = result + "\\\\f";
                    character = iterator.next();
                    break;
                case 0xA: // newline, \n
                    result = result + "\\\\n";
                    character = iterator.next();
                    break;
                case 0xD: // carriage return, \r
                    result = result + "\\\\n";
                    character = iterator.next();
                    break;
                case 0x9:// tab, \t
                    result = result + "\\\\t";
                    character = iterator.next();
                    break;
                case '"':
                    result = result + "\\\"";
                    character = iterator.next();
                    break;
                default:
                    result = result + character;
                    character = iterator.next();
                    break;
            }

        }
        return result.toString();
    }
}
