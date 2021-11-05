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
import java.net.HttpURLConnection;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnableRuleMigrationSupport
class CustomResourceLoadAndCreateTest {
  @Rule
  public KubernetesServer server = new KubernetesServer();

@Test
@DisplayName("Should Create CronTab CRD")
void testCronTabCrd() throws IOException {
  // Given
  KubernetesClient client = server.getClient();
  CustomResourceDefinition cronTabCrd = client.apiextensions().v1()
    .customResourceDefinitions()
    .load(new BufferedInputStream(new FileInputStream("src/test/resources/crontab-crd.yml")))
    .get();
  server.expect().post()
    .withPath("/apis/apiextensions.k8s.io/v1/customresourcedefinitions")
    .andReturn(HttpURLConnection.HTTP_OK, cronTabCrd)
    .once();

  // When
  CustomResourceDefinition createdCronTabCrd = client.apiextensions().v1()
    .customResourceDefinitions()
    .create(cronTabCrd);

  // Then
  assertNotNull(createdCronTabCrd);
}
}
