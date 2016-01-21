import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by diego on 18/01/16.
 */
public class MainClass {

    public  static void main(String ... args) throws DockerCertificateException, DockerException, InterruptedException {
        DockerClient docker = new DefaultDockerClient("unix:///var/run/docker.sock");
        //docker.pull("postgres");

        final String[] ports = {"5432"};
        final Map<String, List<PortBinding>> portBindings = new HashMap<String, List<PortBinding>>();
        for (String port : ports) {
            List<PortBinding> hostPorts = new ArrayList<PortBinding>();
            hostPorts.add(PortBinding.of("0.0.0.0", port));
            portBindings.put(port, hostPorts);
        }

        final HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

        // Create container with exposed ports
        final ContainerConfig containerConfig = ContainerConfig.builder()
                .hostConfig(hostConfig)
                .image("postgres").exposedPorts(ports)
                .build();

        final ContainerCreation creation = docker.createContainer(containerConfig);
        final String id = creation.id();

        // Inspect container
        final ContainerInfo info = docker.inspectContainer(id);

        // Start container
        docker.startContainer(id);

        // Kill container
        docker.killContainer(id);

        // Remove container
        docker.removeContainer(id);

        System.out.println(docker);
    }
}
