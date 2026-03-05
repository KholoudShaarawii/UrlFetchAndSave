import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class ThreadDownloader {

    public static void main(String[] args) {


        /// io bound (NETWORK)

        Path location = Path.of("C:\\Users\\kholo\\Downloads\\ThreadDownloader\\urls.txt");

        if (Files.exists(location)) {
            System.out.println("found");
        } else {
            System.out.println("notFound");
        }

        long start = System.nanoTime();

        try {
            List<String> links = Files.readAllLines(location, StandardCharsets.UTF_8);
            ExecutorService executorservie = Executors.newFixedThreadPool(4); //4 workers

            HttpClient httpClient = HttpClient.newBuilder().build(); //connectionPool

            Path directory = Path.of("C:\\Users\\kholo\\Downloads\\ThreadDownloader\\downloads");// create downloads folder
            Path folderDirectory = Files.createDirectories(directory);
            for (String link : links) {


                Future<?> future = executorservie.submit(() -> {
                    System.out.println("founded links" + " " + link + " " + "working with" + Thread.currentThread().getName());

                    URI uri = URI.create(link);

                    HttpRequest httpRequest = HttpRequest.newBuilder(uri).GET().build(); // 10 links = 10 requests =10 users

                    HttpResponse<byte[]> response = null;
                    try {
                        response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    Integer statusCode = response.statusCode();
                    byte[] body = response.body();
                    HttpHeaders httpHeaders = response.headers();

                    String type = httpHeaders.firstValue("content-type").orElse("?");

                    System.out.println("statusCode" + statusCode + " " + "body" + body.length + " linkType:" + " " + type);

                    try {
                        /// io bound (DISK)

                        /*
                        for (String names : links) {
                            URI uriName = URI.create(names);
                            String path = uriName.getPath();
                            Path fileName = Path.of(path);
                            Path accessFileName = fileName.getFileName();
                            Path filePath = folderDirectory.resolve(accessFileName);
                            //bytes from ram to disk

                            Files.write(filePath, body);
                        }==> connectionPool ??*/
                        String path = uri.getPath();

                        String fileNameStr;
                        if (path == null || path.isBlank() || path.endsWith("/")) {
                            fileNameStr = "modifiedName";
                        } else {
                            fileNameStr = Path.of(path).getFileName().toString();
                        }

                        fileNameStr = fileNameStr.replaceAll("[\\\\/:*?\"<>|]", "_");

                        Path filePath = folderDirectory.resolve(fileNameStr);
                        Files.write(filePath, body);


                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                }  );
            }    executorservie.shutdown(); // Main thread waits until all worker threads finish their tasks.
            int i = 120;
            executorservie.awaitTermination(  i, TimeUnit.SECONDS);

        } catch (IOException |  InterruptedException |IllegalStateException exception) {
            System.out.println(exception.getMessage());
        }

        long end = System.nanoTime();
        long totalTime = end - start;
        double toSecond = totalTime / 1_000_000_000.0;
        System.out.println("totalTime is" + toSecond);



    }
}


