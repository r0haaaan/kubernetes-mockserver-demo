package io.fabric8.demo.kubernetes.customresource;

public class CronTabSpec {
    public String getCronSpec() {
        return cronSpec;
    }

    public void setCronSpec(String cronSpec) {
        this.cronSpec = cronSpec;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getReplicas() {
        return replicas;
    }

    public void setReplicas(int replicas) {
        this.replicas = replicas;
    }

    private String cronSpec;
    private String image;
    private int replicas;

    @Override
    public String toString() {
        return "CronTabSpec{" +
          "replicas=" + replicas  +
          ", cronSpec='" + cronSpec + "'" +
          ", image='" + image + "'" +
          "}";
    }
}