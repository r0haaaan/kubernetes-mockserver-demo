package io.fabric8.demo.kubernetes.mockserver;

import io.fabric8.demo.kubernetes.customresource.CronTab;
import io.fabric8.demo.kubernetes.customresource.CronTabList;
import io.fabric8.demo.kubernetes.customresource.DoneableCronTab;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import org.junit.Rule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;

import static io.fabric8.demo.kubernetes.customresource.CronTabFactory.getCronTab;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnableRuleMigrationSupport
public class CustomResourceCrudMockTest {
    @Rule
    public KubernetesServer crudServer = new KubernetesServer(true, true);

    @Test
    @DisplayName("Should list all CronTab custom resources")
    public void testCronTabCrud() {
        // Given
        KubernetesClient client = crudServer.getClient();

        CustomResourceDefinition cronTabCrd = client.customResourceDefinitions()
                .load(getClass().getResourceAsStream("/crontab-crd.yml")).get();
        MixedOperation<CronTab, CronTabList, DoneableCronTab, Resource<CronTab, DoneableCronTab>> cronTabClient = client
                .customResources(cronTabCrd, CronTab.class, CronTabList.class, DoneableCronTab.class);

        // When
        CronTab createdCronTab = cronTabClient.inNamespace("default").create(getCronTab());
        CronTabList cronTabList = cronTabClient.inNamespace("default").list();

        // Then
        assertNotNull(createdCronTab);
        assertNotNull(cronTabList);
        assertEquals(1, cronTabList.getItems().size());
    }
}
