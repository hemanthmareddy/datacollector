/**
 * Copyright 2016 StreamSets Inc.
 *
 * Licensed under the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.streamsets.pipeline.lib.salesforce;

import com.streamsets.pipeline.api.ConfigDef;
import com.streamsets.pipeline.api.ListBeanModel;
import com.streamsets.pipeline.lib.el.RecordEL;
import com.streamsets.pipeline.lib.el.TimeEL;
import com.streamsets.pipeline.lib.el.TimeNowEL;

import java.util.List;

public class ForceTargetConfigBean extends ForceConfigBean {
  @ConfigDef(
      required = true,
      type = ConfigDef.Type.BOOLEAN,
      defaultValue = "true",
      label = "Use Bulk API",
      description = "If enabled, records will be read and written via the Salesforce Bulk API, " +
          "otherwise, the Salesforce SOAP API will be used.",
      displayPosition = 50,
      group = "FORCE"
  )
  public boolean useBulkAPI;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.STRING,
      elDefs = {RecordEL.class, TimeEL.class, TimeNowEL.class},
      evaluation = ConfigDef.Evaluation.EXPLICIT,
      defaultValue = "${record:attribute('sobjectType')}",
      label = "SObject Type",
      description = "SObject Type - can be literal, such as 'Account', or an expression, like '${record:attribute('type')'}",
      displayPosition = 60,
      group = "FORCE"
  )
  public String sObjectNameTemplate;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.MODEL,
      label = "Field Mapping",
      description = "Mapping from record fields to Salesforce field names.",
      displayPosition = 70,
      group = "FORCE"
  )
  @ListBeanModel
  public List<ForceFieldMapping> fieldMapping;

}
