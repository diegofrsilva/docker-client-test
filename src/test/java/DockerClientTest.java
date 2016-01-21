import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by diego on 18/01/16. Tests using the spotfy docker client API. The tests were made just
 * to see how the API works.
 *
 */
public class DockerClientTest {

    private DefaultDockerClient docker;
    private String containerId;

    @Before
    public void before() throws DockerException, InterruptedException {
        this.docker = new DefaultDockerClient("unix:///var/run/docker.sock");

        String[] ports = {"5432"};
        Map<String, List<PortBinding>> portBindings = new HashMap<String, List<PortBinding>>();
        for (String port : ports) {
            List<PortBinding> hostPorts = new ArrayList<PortBinding>();
            hostPorts.add(PortBinding.of("0.0.0.0", port));
            portBindings.put(port, hostPorts);
        }

        HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

        // Create container with exposed ports
        ContainerConfig containerConfig = ContainerConfig.builder()
                .env("POSTGRES_PASSWORD=changeit")
                .hostConfig(hostConfig)
                .image("postgres")
                .exposedPorts(ports)
                .build();

        final ContainerCreation creation = docker.createContainer(containerConfig);
        this.containerId = creation.id();

        // Inspect container
        final ContainerInfo info = docker.inspectContainer(containerId);

        // Start container
        docker.startContainer(containerId);
    }

    @After
    public void after() throws DockerException, InterruptedException {
        // Kill container
        docker.killContainer(containerId);

        // Remove container
        docker.removeContainer(containerId);
    }

    @Test
    public void testPostgresConnection() throws SQLException, InterruptedException, DockerException {
        /**
        * Work around to wait the container initialization. I know, its a bad ideia... I know, but for now... :(
        * */
        Thread.sleep(10000);
        // TODO: Look if there is way to verify the container state maybe -> docker.inspectContainer(containerId).state().running()
       
        Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "changeit");
        boolean executed = connection.prepareStatement("SELECT 1").execute();
        assertTrue(executed);
    }
}
