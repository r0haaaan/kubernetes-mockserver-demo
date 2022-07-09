package io.fabric8.demo.kubernetes.mockserver;

import io.fabric8.demo.kubernetes.customresource.CronTab;
import io.fabric8.demo.kubernetes.customresource.CronTabList;
import io.fabric8.kubernetes.api.model.DefaultKubernetesResourceList;
import io.fabric8.kubernetes.api.model.WatchEvent;
import io.fabric8.kubernetes.api.model.WatchEventBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
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
import static org.assertj.core.api.Assertions.assertThat;

@EnableRuleMigrationSupport
class CustomResourceMockTest {
    @Rule
    public KubernetesServer server = new KubernetesServer();

    @Test
    @DisplayName("Should list all CronTab custom resources")
    void testCronTabList() {
        // Given
        server.expect().get()
          .withPath("/apis/stable.example.com/v1/namespaces/default/crontabs")
          .andReturn(HttpURLConnection.HTTP_OK, getCronTabList())
          .once();
        KubernetesClient client = server.getClient();

        MixedOperation<CronTab, CronTabList, Resource<CronTab>> cronTabClient = client
                .resources(CronTab.class, CronTabList.class);

        // When
        CronTabList cronTabList = cronTabClient.inNamespace("default").list();

        // Then
        assertThat(cronTabList)
            .extracting(DefaultKubernetesResourceList::getItems)
            .asList()
            .hasSize(1);
    }

    @Test
    @DisplayName("Should watch all CronTab custom resources")
    void testCronTabWatch() throws InterruptedException {
        // Given
        server.expect().withPath("/apis/stable.example.com/v1/namespaces/default/crontabs?allowWatchBookmarks=true&watch=true")
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
        MixedOperation<CronTab, CronTabList, Resource<CronTab>> cronTabClient = client
                .resources(CronTab.class, CronTabList.class);

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
            public void onClose(WatcherException e) { }
        });

        // Then
        assertThat(eventRecieved.await(1, TimeUnit.SECONDS)).isTrue();
        assertThat(eventRecieved.getCount()).isZero();
        watch.close();
    }

}
