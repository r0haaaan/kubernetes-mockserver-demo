package io.fabric8.demo.kubernetes.customresource;

import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;

import java.util.Collections;

public class CronTabFactory {
    public static CronTabList getCronTabList() {
        CronTabList cronTabList = new CronTabList();
        cronTabList.setItems(Collections.singletonList(getCronTab()));

        return cronTabList;
    }

    public static CronTab getCronTab() {
        CronTab cronTab = new CronTab();
        CronTabSpec cronTabSpec = new CronTabSpec();
        cronTabSpec.setCronSpec("* * * * */5");
        cronTabSpec.setImage("my-awesome-cron-image");

        cronTab.setMetadata(new ObjectMetaBuilder().withName("my-new-cron-object").build());
        cronTab.setSpec(cronTabSpec);
        return cronTab;
    }
}
