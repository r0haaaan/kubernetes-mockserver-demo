package io.fabric8.demo.kubernetes.customresource;

import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

@Version("v1")
@Group("stable.example.com")
public class CronTab extends CustomResource<CronTabSpec, CronTabStatus> implements Namespaced {
}
