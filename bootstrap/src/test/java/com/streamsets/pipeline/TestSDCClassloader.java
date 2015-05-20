/**
 * (c) 2014 StreamSets, Inc. All rights reserved. May not
 * be copied, modified, or distributed in whole or part without
 * written consent of StreamSets, Inc.
 */
package com.streamsets.pipeline;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

public class TestSDCClassloader {

  @Test
  @SuppressWarnings("unchecked")
  public void testServices() throws Exception {
    Assert.assertTrue(SDCClassLoader.isClassInList(SDCClassLoader.SERVICES_PREFIX +
      "org.apache.hadoop.fs.FileSystem", Arrays.asList("org.apache.hadoop.fs.")));
  }

  private static class CallStoringURLClassLoader extends SDCClassLoader {
    final List<String> calls = new ArrayList<>();

    public CallStoringURLClassLoader(ClassLoader parent, String systemClasses, String appClasses) {
      super("test", "somecl", Arrays.<URL>asList(), parent,
        new String[0], systemClasses, appClasses);
    }

    @Override
    public URL findResource(String name) {
      calls.add("findResource " + name);
      return super.findResource(name);
    }

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
      calls.add("findResources " + name);
      return super.findResources(name);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve)
      throws ClassNotFoundException {
      calls.add("loadClass " + name);
      return super.loadClass(name, resolve);
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testClassLoaderOrder() throws Exception {
    CallStoringURLClassLoader parent = new CallStoringURLClassLoader(new URLClassLoader(new URL[0]), "", "");
    CallStoringURLClassLoader stage = new CallStoringURLClassLoader(parent, "sys.", "app.");
    // sys should be delegated to the parent
    doLoadClass(Arrays.asList("loadClass sys.Dummy"), stage, "sys.Dummy");
    // other should be delegated to the parent
    doLoadClass(Arrays.asList("loadClass other.Dummy"), stage, "other.Dummy");
    // app should be delegated to the parent
    doLoadClass(Arrays.<String>asList(), stage, "app.Dummy");

    doGetResource(Arrays.<String>asList("findResource /META-INF/services/sys.Dummy",
      "findResource META-INF/services/sys.Dummy"), stage, SDCClassLoader.SERVICES_PREFIX + "sys.Dummy");
    doGetResource(Arrays.<String>asList("findResource /META-INF/services/other.Dummy",
      "findResource META-INF/services/other.Dummy"), stage, SDCClassLoader.SERVICES_PREFIX + "other.Dummy");
    doGetResource(Arrays.<String>asList(), stage, SDCClassLoader.SERVICES_PREFIX + "app.Dummy");

    doGetResources(Arrays.<String>asList("findResources /META-INF/services/sys.Dummy",
        "findResources /META-INF/services/sys.Dummy"), stage, SDCClassLoader.SERVICES_PREFIX + "sys.Dummy");
    doGetResources(Arrays.<String>asList("findResources /META-INF/services/other.Dummy",
      "findResources /META-INF/services/other.Dummy"), stage, SDCClassLoader.SERVICES_PREFIX + "other.Dummy");
    doGetResources(Arrays.<String>asList(), stage, SDCClassLoader.SERVICES_PREFIX + "app.Dummy");

    doGetResourceAsStream(Arrays.<String>asList("findResource /META-INF/services/sys.Dummy"), stage,
      SDCClassLoader.SERVICES_PREFIX + "sys.Dummy");
    doGetResourceAsStream(Arrays.<String>asList("findResource /META-INF/services/other.Dummy"), stage,
      SDCClassLoader.SERVICES_PREFIX + "other.Dummy");
    doGetResourceAsStream(Arrays.<String>asList(), stage, SDCClassLoader.SERVICES_PREFIX +
      "app.Dummy");

    doGetResource(Arrays.<String>asList("findResource sys.Dummy"), stage, "sys.Dummy");
    doGetResource(Arrays.<String>asList("findResource other.Dummy"), stage, "other.Dummy");
    doGetResource(Arrays.<String>asList(), stage, "app.Dummy");

    doGetResources(Arrays.<String>asList("findResources sys.Dummy", "findResources sys.Dummy"), stage, "sys.Dummy");
    doGetResources(Arrays.<String>asList("findResources other.Dummy", "findResources other.Dummy"), stage, "other.Dummy");
    doGetResources(Arrays.<String>asList(), stage, "app.Dummy");

    doGetResourceAsStream(Arrays.<String>asList(
      "findResource sys.Dummy"), stage, "sys.Dummy");
    doGetResourceAsStream(Arrays.<String>asList(
      "findResource other.Dummy"), stage, "other.Dummy");
    doGetResourceAsStream(Arrays.<String>asList(), stage, "app.Dummy");
  }

  private static void doGetResourceAsStream(List<String> expectedCallsToParent,
                                            SDCClassLoader stage, String name) throws Exception {
    List<String> actualCallsToParent = ((CallStoringURLClassLoader) stage.getParent()).calls;
    Assert.assertNull(stage.getResourceAsStream(name));
    Assert.assertEquals(expectedCallsToParent, actualCallsToParent);
    actualCallsToParent.clear();
  }

  private static void doGetResources(List<String> expectedCallsToParent,
                                     SDCClassLoader stage, String name) throws Exception {
    List<String> actualCallsToParent = ((CallStoringURLClassLoader) stage.getParent()).calls;
    Enumeration<URL> enumeration = stage.getResources(name);
    Assert.assertFalse(enumeration.hasMoreElements());
    Assert.assertEquals(expectedCallsToParent, actualCallsToParent);
    actualCallsToParent.clear();
  }

  private static void doGetResource(List<String> expectedCallsToParent,
                                    SDCClassLoader stage, String name) {
    List<String> actualCallsToParent = ((CallStoringURLClassLoader) stage.getParent()).calls;
    Assert.assertNull(stage.getResource(name));
    Assert.assertEquals(expectedCallsToParent, actualCallsToParent);
    actualCallsToParent.clear();
  }

  private static void doLoadClass(List<String> expectedCallsToParent,
                                  SDCClassLoader stage, String name) {
    List<String> actualCallsToParent = ((CallStoringURLClassLoader) stage.getParent()).calls;
    try {
      stage.loadClass(name);
      Assert.fail("expected ClassNotFoundException");
    } catch (ClassNotFoundException ex) {
      // expected
    }
    Assert.assertEquals(expectedCallsToParent, actualCallsToParent);
    actualCallsToParent.clear();
  }
}
