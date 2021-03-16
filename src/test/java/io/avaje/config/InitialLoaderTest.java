package io.avaje.config;

import org.junit.Test;

import java.util.Properties;

import static io.avaje.config.InitialLoader.Source.RESOURCE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class InitialLoaderTest {

  @Test
  public void load() {

    String userName = System.getProperty("user.name");
    String userHome = System.getProperty("user.home");

    InitialLoader loader = new InitialLoader();
    loader.loadProperties("test-properties/application.properties", RESOURCE);
    loader.loadYaml("test-properties/application.yaml", RESOURCE);

    loader.loadProperties("test-properties/one.properties", RESOURCE);
    loader.loadYaml("test-properties/foo.yml", RESOURCE);

    Properties properties = loader.eval();

    assertEquals("fromProperties", properties.getProperty("app.fromProperties"));
    assertEquals("Two", properties.getProperty("app.two"));

    assertEquals("bart", properties.getProperty("eval.withDefault"));
    assertEquals(userName, properties.getProperty("eval.name"));
    assertEquals(userHome + "/after", properties.getProperty("eval.home"));

    assertEquals("before|Beta|after", properties.getProperty("someOne"));
    assertEquals("before|Two|after", properties.getProperty("someTwo"));
  }

  @Test
  public void loadWithExtensionCheck() {

    InitialLoader loader = new InitialLoader();
    loader.loadFileWithExtensionCheck("test-dummy.properties");
    loader.loadFileWithExtensionCheck("test-dummy.yml");
    loader.loadFileWithExtensionCheck("test-dummy2.yaml");

    Properties properties = loader.eval();
    assertThat(properties.getProperty("dummy.yaml.bar")).isEqualTo("baz");
    assertThat(properties.getProperty("dummy.yml.foo")).isEqualTo("bar");
    assertThat(properties.getProperty("dummy.properties.foo")).isEqualTo("bar");
  }


  @Test
  public void loadYaml() {

    InitialLoader loader = new InitialLoader();
    loader.loadYaml("test-properties/foo.yml", RESOURCE);
    Properties properties = loader.eval();

    assertThat(properties.getProperty("Some.Other.pass")).isEqualTo("someDefault");
  }

  @Test
  public void loadProperties() {

    System.setProperty("eureka.instance.hostname", "host1");
    System.setProperty("server.port", "9876");

    InitialLoader loader = new InitialLoader();
    loader.loadProperties("test-properties/one.properties", RESOURCE);
    Properties properties = loader.eval();

    assertThat(properties.getProperty("hello")).isEqualTo("there");
    assertThat(properties.getProperty("name")).isEqualTo("Rob");
    assertThat(properties.getProperty("statusPageUrl")).isEqualTo("https://host1:9876/status");
    assertThat(properties.getProperty("statusPageUrl2")).isEqualTo("https://aaa:9876/status2");
    assertThat(properties.getProperty("statusPageUrl3")).isEqualTo("https://aaa:89/status3");
    assertThat(properties.getProperty("statusPageUrl4")).isEqualTo("https://there:9876/name/Rob");
  }

  @Test
  public void splitPaths() {
    InitialLoader loader = new InitialLoader();
    assertThat(loader.splitPaths("one two three")).contains("one", "two", "three");
    assertThat(loader.splitPaths("one,two,three")).contains("one", "two", "three");
    assertThat(loader.splitPaths("one;two;three")).contains("one", "two", "three");
    assertThat(loader.splitPaths("one two,three;four,five six")).contains("one", "two", "three", "four", "five", "six");
  }

  @Test
  public void loadViaCommandLine_whenNotValid() {
    InitialLoader loader = new InitialLoader();
    loader.loadViaCommandLine(new String[]{"-p", "8765"});
    assertEquals(0, loader.size());
    loader.loadViaCommandLine(new String[]{"-port", "8765"});
    assertEquals(0, loader.size());

    loader.loadViaCommandLine(new String[]{"-port"});
    loader.loadViaCommandLine(new String[]{"-p", "ort"});
    assertEquals(0, loader.size());

    loader.loadViaCommandLine(new String[]{"-p", "doesNotExist.yaml"});
    assertEquals(0, loader.size());
  }

  @Test
  public void loadViaCommandLine_localFile() {
    InitialLoader loader = new InitialLoader();
    loader.loadViaCommandLine(new String[]{"-p", "test-dummy2.yaml"});
    assertEquals(1, loader.size());
  }

  @Test
  public void loadProfilesResources() {
    System.setProperty("config.profiles.active", "dev,dev2");

    InitialLoader loader = new InitialLoader();

    Properties properties = loader.load();

    assertEquals(properties.get("service.service1.name"), "foo-service1");
    assertEquals(properties.get("service.service2.name"), "foo-service2");
  }
}
