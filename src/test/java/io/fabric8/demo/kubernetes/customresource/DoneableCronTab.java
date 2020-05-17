package io.fabric8.demo.kubernetes.customresource;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;


public class DoneableCronTab extends CustomResourceDoneable<CronTab> {
    public DoneableCronTab(CronTab resource, Function function) { super(resource, function); }
}
