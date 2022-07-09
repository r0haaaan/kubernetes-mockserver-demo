package io.fabric8.demo.kubernetes.mockserver;

import io.fabric8.kubernetes.api.model.Condition;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.WatchEvent;
import io.fabric8.kubernetes.api.model.WatchEventBuilder;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Kind;
import io.fabric8.kubernetes.model.annotation.Version;
import org.junit.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;

import java.net.HttpURLConnection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@EnableRuleMigrationSupport
public class TestKafkaHelperUtilFourth {

  @Rule
  public KubernetesServer server = new KubernetesServer(true, false);

  @Test
  @DisplayName("Should watch all custom resources")
  public void testWatch() throws InterruptedException {
    // Given
    server.expect().withPath("/apis/custom.example.com/v1/namespaces/default/useracls?watch=true")
        .andUpgradeToWebSocket()
        .open()
        .waitFor(10L)
        .andEmit(new WatchEvent(getUserACL("test-resource"), "ADDED"))
        .waitFor(10L)
        .andEmit(new WatchEventBuilder()
            .withNewStatusObject()
            .withMessage("410 - the event requested is outdated")
            .withCode(HttpURLConnection.HTTP_GONE)
            .endStatusObject()
            .build()).done().once();
    KubernetesClient client = server.getClient();
    MixedOperation<
        UserACL,
        KubernetesResourceList<UserACL>,
        Resource<UserACL>>
        userAclClient = client.resources(UserACL.class);

    // When
    CountDownLatch eventRecieved = new CountDownLatch(1);
    KubernetesDeserializer.registerCustomKind("custom.example.com/v1", "UserACL", UserACL.class);
    Watch watch = userAclClient.inNamespace("default").watch(new Watcher<UserACL>() {
      @Override
      public void eventReceived(Action action, UserACL userAcl) {
        if (action.name().contains("ADDED"))
          eventRecieved.countDown();
      }

      @Override
      public void onClose(WatcherException e) { }
    });

    // Then
    eventRecieved.await(30, TimeUnit.SECONDS);
    Assertions.assertEquals(0, eventRecieved.getCount());
    watch.close();
  }

  private UserACL getUserACL(String resourceName) {
    UserACLSpec spec = new UserACLSpec();
    spec.setUserName("test-user-name");

    UserACL createdUserACL = new UserACL();
    createdUserACL.setMetadata(
        new ObjectMetaBuilder().withName(resourceName).build());
    createdUserACL.setSpec(spec);

    Condition condition = new Condition();
    condition.setMessage("Last reconciliation succeeded");
    condition.setReason("Successful");
    condition.setStatus("True");
    condition.setType("Successful");
    UserACLStatus status = new UserACLStatus();
    status.setCondition(new Condition[]{condition});
    createdUserACL.setStatus(status);

    return createdUserACL;
  }

  @Group("custom.example.com")
  @Version("v1")
  @Kind("UserACL")
  public static final class UserACL extends CustomResource<UserACLSpec, UserACLStatus> implements Namespaced { }

  public static final class UserACLSpec {
    private String userName;

    public UserACLSpec() {}

    public String getUserName() {
      return userName;
    }

    public void setUserName(String userName) {
      this.userName = userName;
    }
  }
  public static final class UserACLStatus {
    Condition[] condition;

    public UserACLStatus() {};

    public Condition[] getCondition() {
      return condition;
    }

    public void setCondition(Condition[] condition) {
      this.condition = condition;
    }
  }
}
