package io.fabric8.demo.kubernetes.mockserver;

import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import org.junit.Rule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnableRuleMigrationSupport
class CustomResourceLoadAndCreateCrudTest {
  @Rule
  public KubernetesServer server = new KubernetesServer(true, true);

  @Test
  @DisplayName("Should Create CronTab CRD")
  void testCronTabCrd() throws IOException {
    // Given
    KubernetesClient client = server.getClient();
    CustomResourceDefinition cronTabCrd = client.apiextensions().v1()
      .customResourceDefinitions()
      .load(new BufferedInputStream(new FileInputStream("src/test/resources/crontab-crd.yml")))
      .get();

    // When
    CustomResourceDefinition createdCronTabCrd = client.apiextensions().v1()
        .customResourceDefinitions()
        .resource(cronTabCrd)
        .create();

    // Then
    assertNotNull(createdCronTabCrd);
  }
}
