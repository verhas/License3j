package javax0.license3j.hardware;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Obtain the unique and immutable cloud instance/machine id, based on the dedicated, non-routable 169.254.169.254 ip address,
 * as implemented by various cloud providers.
 */
public enum CloudProvider {

    /**
     * Obtain a Azure instance id.
     * Refer to <a href="https://docs.microsoft.com/en-us/azure/virtual-machines/windows/instance-metadata-service?tabs=windows">Azure documentation</a>
     * Refer to <a href="https://gist.github.com/dreamorosi/50cbfd622b478c2433602c16b7321c5d">Examples</a>
     */
    Azure {
        @Override
        String getInstanceId() {
            return instanceIdFor("http://169.254.169.254/metadata/instance/compute/vmId?api-version=2021-02-01&format=text", "Metadata", "true");
        }
    },

    /**
     * Obtain a AWS instance id.
     * Refer to <a href="https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instancedata-data-retrieval.html">AWS documentation</a>
     * Refer to <a href="https://gist.github.com/dreamorosi/50cbfd622b478c2433602c16b7321c5d">Examples</a>
     * Refer to <a href="https://stackoverflow.com/questions/625644/how-to-get-the-instance-id-from-within-an-ec2-instance">Stack overflow</a>
     */
    AWS {
        @Override
        String getInstanceId() {
            return instanceIdFor("http://169.254.169.254/latest/meta-data/instance-id", "Metadata", "true");
        }
    },

    /**
     * Obtain a Google cloud instance id.
     * <p>
     * Refer to <a href="https://cloud.google.com/compute/docs/metadata/overview">Google cloud documentation</a>
     * Refer to <a href="https://cloud.yandex.com/en/docs/compute/operations/vm-info/get-info">Examples</a>
     * Refer to <a href="https://stackoverflow.com/questions/31688646/get-the-name-or-id-of-the-current-google-compute-instance">Stack overflow</a>
     */
    Google {
        @Override
        String getInstanceId() {
            return instanceIdFor("http://169.254.169.254/computeMetadata/v1/instance?alt=text", "Metadata-Flavor", "Google");
        }
    };


    abstract String getInstanceId();


    private static String instanceIdFor(String instanceIdUrl, String... headers) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request =
                    HttpRequest
                            .newBuilder(new URI(instanceIdUrl))
                            .headers(headers)
                            .GET()
                            .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (InterruptedException | URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
