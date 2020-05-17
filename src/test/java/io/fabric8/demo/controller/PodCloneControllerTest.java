package io.fabric8.demo.controller;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.PodListBuilder;
import io.fabric8.kubernetes.api.model.Status;
import io.fabric8.kubernetes.api.model.StatusBuilder;
import io.fabric8.kubernetes.api.model.WatchEvent;
import io.fabric8.kubernetes.api.model.WatchEventBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.SharedInformerFactory;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;

import java.net.HttpURLConnection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnableRuleMigrationSupport
public class PodCloneControllerTest {
    @Rule
    public KubernetesServer server = new KubernetesServer();

    @Test
    @DisplayName("Should create clone pods")
    public void testReconcile() throws InterruptedException {
        // Given
        Pod pod = new PodBuilder().withNewMetadata().withName("pod1").withNamespace("test").endMetadata().build();
        server.expect().post().withPath("/api/v1/namespaces/test/pods")
                .andReturn(HttpURLConnection.HTTP_CREATED, new PodBuilder().withNewMetadata().withName("pod1-clone").endMetadata().build())
                .once();
        KubernetesClient client = server.getClient();

        SharedInformerFactory informerFactory = client.informers();
        SharedIndexInformer<Pod> podSharedIndexInformer = informerFactory.sharedIndexInformerFor(Pod.class, PodList.class, 10 * 60 * 1000);
        PodCloneController podSetController = new PodCloneController(client, podSharedIndexInformer, "test");

        // When
        podSetController.reconcile(pod);

        // Then
        RecordedRequest recordedRequest = server.getLastRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertTrue(recordedRequest.getBody().readUtf8().contains("clone"));
    }
}
