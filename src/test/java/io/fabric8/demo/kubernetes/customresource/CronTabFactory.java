package io.fabric8.demo.kubernetes.customresource;

import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.ListMeta;
import io.fabric8.kubernetes.api.model.ListMetaBuilder;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;

import java.util.Collections;
import java.util.List;

public class CronTabFactory {
    public static KubernetesResourceList<CronTab> getCronTabList() {
        return new KubernetesResourceList<CronTab>() {
            @Override
            public ListMeta getMetadata() {
                return new ListMetaBuilder().build();
            }

            @Override
            public List<CronTab> getItems() {
                return Collections.singletonList(getCronTab());
            }
        };
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
