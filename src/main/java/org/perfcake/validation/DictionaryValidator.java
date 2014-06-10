/*
 * -----------------------------------------------------------------------\
 * PerfCake
 *  
 * Copyright (C) 2010 - 2013 the original author or authors.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -----------------------------------------------------------------------/
 */
package org.perfcake.validation;

import org.apache.log4j.Logger;
import org.perfcake.message.Message;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Dictionary validator can create a dictionary of valid responses and use this to validate them in another run.
 * It is also possible to create the dictionary manually, however, this is to complicated task and we always
 * recommend running the validation in record mode first. Any manual changes can be done later.
 * Dictionary validator creates an index file and a separate file for each response. A writable directory must
 * be specified. The default index file name can be redefined. The response file names are based on hash codes of
 * the original messages. Empty, null or equal messages will overwrite the file but this is not the intended use
 * of this validator. Index file is never overwritten, if you really insist on recreating it, please rename or
 * delete the file manually (this is for safety reasons).
 *
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public class DictionaryValidator implements MessageValidator {

   private final Logger log = Logger.getLogger(ValidationManager.class);

   private String dictionaryDirectory;
   private String dictionaryIndex = "index";
   private boolean record = false;
   private boolean indexChecked = false;
   private Properties indexCache;

   /**
    * Escapes = and : in the payload string.
    *
    * @param payload The payload string to be escaped.
    * @return Escaped payload.
    */
   private String escapePayload(String payload) {
      return payload.replaceAll("=", "\\=").replaceAll(":", "\\:");
   }

   /**
    * Records the correct response.
    *
    * @param originalMessage The original message.
    * @param response The response that is considered correct.
    * @throws ValidationException If any of the disk operations fails.
    */
   private void recordResponse(final Message originalMessage, final Message response) throws ValidationException {
      String responseHashCode = Integer.toString(response.getPayload().toString().hashCode());

      try (FileWriter indexWriter = new FileWriter(getIndexFile()); FileWriter responseWriter = new FileWriter(new File(dictionaryDirectory, responseHashCode))) {
         indexWriter.append(escapePayload(originalMessage.getPayload().toString()));
         indexWriter.append("=");
         indexWriter.append(responseHashCode);

         responseWriter.write(response.getPayload().toString());
      } catch (IOException e) {
         throw new ValidationException(String.format("Cannot record correct response for message '%s': ", response.getPayload().toString()), e);
      }
   }

   /**
    * Reads the index into memory, or return the previously read index.
    *
    * @return the response index
    * @throws ValidationException If any of the disk operations fails.
    */
   private Properties getIndexCache() throws ValidationException {
      if (indexCache == null) {
         indexCache = new Properties();
         try (FileReader indexReader = new FileReader(getIndexFile())) {
            indexCache.load(indexReader);
         } catch (IOException e) {
            throw new ValidationException(String.format("Unable to load index file '%s': ", getIndexFile().getAbsolutePath()), e);
         }
      }

      return indexCache;
   }

   /**
    * Validates the response against the previously recorded correct responses.
    *
    * @param originalMessage The original message.
    * @param response The response to be validated.
    * @return True if and only if the validation passed.
    * @throws ValidationException If any of the disk operations fails.
    */
   private boolean validateResponse(Message originalMessage, Message response) throws ValidationException {
      String responseHashCode = getIndexCache().getProperty(escapePayload(originalMessage.getPayload().toString()));
      try {
         String newResponse = response != null && response.getPayload() != null ? response.getPayload().toString() : "";
         String responseString = new String(Files.readAllBytes(Paths.get(dictionaryDirectory, responseHashCode)), StandardCharsets.UTF_8);

         return newResponse.equals(responseString);
      } catch (IOException e) {
         throw new ValidationException(String.format("Cannot read correct response from file '%s': ", new File(dictionaryDirectory, responseHashCode).getAbsolutePath()), e);
      }
   }

   /**
    * Gets the file with index of recorded responses.
    * @return The index file.
    */
   private File getIndexFile() {
      return new File(dictionaryDirectory, dictionaryIndex);
   }

   /**
    * Checks whether the index file exists.
    * @return True if and only if the index file exists.
    */
   private boolean indexExists() {
      return (dictionaryDirectory != null && dictionaryIndex != null) && getIndexFile().exists();
   }

   @Override
   public boolean isValid(final Message originalMessage, final Message response) {
      if (!indexChecked && record && indexExists()) {
         // We are in record mode and did not previously check for the index existence. Once this is checked and the index is not present,
         // we never appear here again. If the check did not pass, we will log the error again and again.
         log.error("Error while trying to record responses - index file already exists, overwrite not permitted.");
         return false;
      } else {
         indexChecked = true;

         if (record) {
            try { // in record mode record the answer (considered as correct)
               recordResponse(originalMessage, response);

               return true;
            } catch (ValidationException e) {
               log.error("Error recording correct response: ", e);
            }
         } else {
            try { // in normal mode, validate the answer against already recorded responses
               return validateResponse(originalMessage, response);
            } catch (ValidationException e) {
               log.error("Error validating response: ", e);
            }
         }

      }

      return false;
   }

   public String getDictionaryDirectory() {
      return dictionaryDirectory;
   }

   public void setDictionaryDirectory(String dictionaryDirectory) {
      this.dictionaryDirectory = dictionaryDirectory;
   }

   public String getDictionaryIndex() {
      return dictionaryIndex;
   }

   public void setDictionaryIndex(String dictionaryIndex) {
      this.dictionaryIndex = dictionaryIndex;
   }

   public boolean isRecord() {
      return record;
   }

   public void setRecord(boolean record) {
      this.record = record;
   }
}