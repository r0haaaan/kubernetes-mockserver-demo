package io.fabric8.demo.kubernetes.mockserver;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import org.junit.Rule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;

import java.net.HttpURLConnection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnableRuleMigrationSupport
class DeploymentMockTest {
    @Rule
    public KubernetesServer server = new KubernetesServer();

    @Test
    @DisplayName("Should create, update and delete Deployment")
    void testDeploymentCrud() {
        // Given
        DeploymentBuilder deploymentBuilder = new DeploymentBuilder()
                .withNewMetadata().withName("deploy1").endMetadata();
        server.expect().post().withPath("/apis/apps/v1/namespaces/ns1/deployments")
                .andReturn(HttpURLConnection.HTTP_CREATED, deploymentBuilder.build()).once();
        server.expect().get().withPath("/apis/apps/v1/namespaces/ns1/deployments/deploy1")
                .andReturn(HttpURLConnection.HTTP_OK, deploymentBuilder.build()).times(2);
        server.expect().patch().withPath("/apis/apps/v1/namespaces/ns1/deployments/deploy1")
                .andReturn(HttpURLConnection.HTTP_OK, deploymentBuilder.editMetadata()
                        .addToLabels("foo", "bar").endMetadata().build()).once();
        server.expect().delete().withPath("/apis/apps/v1/namespaces/ns1/deployments/deploy1")
                .andReturn(HttpURLConnection.HTTP_OK, deploymentBuilder.build()).once();

        KubernetesClient client = server.getClient();

        // When
        client.apps().deployments().inNamespace("ns1").create(deploymentBuilder.build());
        Deployment deployment = client.apps().deployments().inNamespace("ns1").withName("deploy1")
          .edit(d -> new DeploymentBuilder(d)
                .editMetadata().addToLabels("foo", "bar").endMetadata().build());
        Boolean isDeleted = client.apps().deployments().inNamespace("ns1").withName("deploy1").delete();

        // Then
        assertFalse(deployment.getMetadata().getLabels().isEmpty());
        assertEquals("bar", deployment.getMetadata().getLabels().get("foo"));
        assertTrue(isDeleted);
    }
}
