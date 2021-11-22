/*
 * Copyright (c) 2017. Open Text Corporation. All Rights Reserved.
 */

package com.emc.documentum.rest.extension.context.jc;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(basePackages = "com.emc.documentum.rest.extension",
        excludeFilters = { @ComponentScan.Filter(type = FilterType.CUSTOM,
        value = { com.emc.documentum.rest.context.ComponentScanExcludeFilter.class }) })
public class ExtensionContextConfig {

}
