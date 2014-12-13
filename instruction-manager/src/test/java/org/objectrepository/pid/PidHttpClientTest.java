package org.objectrepository.pid;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/or-manager.xml", "classpath:META-INF/spring/application-context.xml"})

public class PidHttpClientTest {

    @Autowired
    PidHttpClient pidHttpClient;

    @Value("#{clientProperties['pidwebservice.endpoint']}")
    private String endpoint;

    @Value("#{clientProperties['pidwebservice.wskey']}")
    private String wskey;

    @Test
    public void getPidOr() {

        // We cannot mock the PID webservice now. So
        // only run this test within an integration environment.

        if (System.getProperty("environment", "test").equalsIgnoreCase("integration")) {

            String pid1 = pidHttpClient.getPid(null, null, "12345", "a local identifier 1", "A resolve URL");
            Assert.assertNotNull(pid1);

            String pid2 = pidHttpClient.getPid(null, null, "12345", "a local identifier 2", "A resolve URL");
            Assert.assertNotNull(pid2);
            Assert.assertFalse(pid1.equals(pid2));

            String pid3 = pidHttpClient.getPid(null, null, "12345", "a local identifier 2", "A resolve URL");
            Assert.assertNotNull(pid3);
            Assert.assertTrue(pid3.equals(pid2));
        }
    }

    @Test
    public void getPidCp() {

        // We cannot mock the PID webservice now. So
        // only run this test within the integration environment.

        if (System.getProperty("environment", "test").equalsIgnoreCase("integration")) {

            String cp_url = endpoint;
            String cp_key = wskey;

            String pid1 = pidHttpClient.getPid(cp_url, cp_key, "12345", "a local identifier 1", "A resolve URL");
            Assert.assertNotNull(pid1);

            String pid2 = pidHttpClient.getPid(cp_url, cp_key, "12345", "a local identifier 2", "A resolve URL");
            Assert.assertNotNull(pid2);
            Assert.assertFalse(pid1.equals(pid2));

            String pid3 = pidHttpClient.getPid(cp_url, cp_key, "12345", "a local identifier 2", "A resolve URL");
            Assert.assertNotNull(pid3);
            Assert.assertTrue(pid3.equals(pid2));

        }
    }

}
