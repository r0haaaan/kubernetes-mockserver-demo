package io.fabric8.demo.kubernetes.mockserver;

import io.fabric8.demo.kubernetes.customresource.CronTab;
import io.fabric8.demo.kubernetes.customresource.CronTabList;
import io.fabric8.demo.kubernetes.customresource.CronTabSpec;
import io.fabric8.demo.kubernetes.customresource.DoneableCronTab;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.WatchEvent;
import io.fabric8.kubernetes.api.model.WatchEventBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;
import org.junit.Rule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;

import java.net.HttpURLConnection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static io.fabric8.demo.kubernetes.customresource.CronTabFactory.getCronTab;
import static io.fabric8.demo.kubernetes.customresource.CronTabFactory.getCronTabList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnableRuleMigrationSupport
public class CustomResourceMockTest {
    @Rule
    public KubernetesServer server = new KubernetesServer();

    @Test
    @DisplayName("Should list all CronTab custom resources")
    public void testCronTabList() {
        // Given
        server.expect().get().withPath("/apis/stable.example.com/v1/namespaces/default/crontabs")
                .andReturn(HttpURLConnection.HTTP_OK, getCronTabList()).once();
        KubernetesClient client = server.getClient();

        CustomResourceDefinition cronTabCrd = client.customResourceDefinitions()
                .load(getClass().getResourceAsStream("/crontab-crd.yml")).get();
        MixedOperation<CronTab, CronTabList, DoneableCronTab, Resource<CronTab, DoneableCronTab>> cronTabClient = client
                .customResources(cronTabCrd, CronTab.class, CronTabList.class, DoneableCronTab.class);

        // When
        CronTabList cronTabList = cronTabClient.inNamespace("default").list();

        // Then
        assertNotNull(cronTabList);
        assertEquals(1, cronTabList.getItems().size());
    }

    @Test
    @DisplayName("Should watch all CronTab custom resources")
    public void testCronTabWatch() throws InterruptedException {
        // Given
        server.expect().withPath("/apis/stable.example.com/v1/namespaces/default/crontabs?watch=true")
                .andUpgradeToWebSocket()
                .open()
                .waitFor(10L)
                .andEmit(new WatchEvent(getCronTab(), "ADDED"))
                .waitFor(10L)
                .andEmit(new WatchEventBuilder()
                        .withNewStatusObject()
                        .withMessage("410 - the event requested is outdated")
                        .withCode(HttpURLConnection.HTTP_GONE)
                        .endStatusObject()
                        .build()).done().always();
        KubernetesClient client = server.getClient();
        CustomResourceDefinition cronTabCrd = client.customResourceDefinitions()
                .load(getClass().getResourceAsStream("/crontab-crd.yml")).get();
        MixedOperation<CronTab, CronTabList, DoneableCronTab, Resource<CronTab, DoneableCronTab>> cronTabClient = client
                .customResources(cronTabCrd, CronTab.class, CronTabList.class, DoneableCronTab.class);

        // When
        CountDownLatch eventRecieved = new CountDownLatch(1);
        KubernetesDeserializer.registerCustomKind("stable.example.com/v1", "CronTab", CronTab.class);
        Watch watch = cronTabClient.inNamespace("default").watch(new Watcher<CronTab>() {
            @Override
            public void eventReceived(Action action, CronTab cronTab) {
                if (action.name().contains("ADDED"))
                    eventRecieved.countDown();
            }

            @Override
            public void onClose(KubernetesClientException e) {
            }
        });

        // Then
        eventRecieved.await(1, TimeUnit.SECONDS);
        assertEquals(0, eventRecieved.getCount());
        watch.close();
    }

}
