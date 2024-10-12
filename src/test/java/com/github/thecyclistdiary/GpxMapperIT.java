package com.github.thecyclistdiary;

import files.DefaultGpxRunner;
import files.ExtractedGpxResult;
import map.gpx.DefaultGpxMapper;
import map.gpx.GpxStyler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GpxMapperIT {

    @TempDir
    Path tempDir;

    @Test
    void image_should_be_properly_formatted() throws IOException {
        // Given a gpx file
        Path resourceDirectory = Paths.get("src", "test", "resources");


        // When generating image, with proper styling
        GpxStyler gpxStyler = new GpxStyler.builder()
                .withGraphChartPadding(10)
                .build();
        DefaultGpxMapper gpxMapper = new DefaultGpxMapper.builder()
                .withHeight(800)
                .withWidth(1200)
                .withChartHeight(100)
                .withGpxStyler(gpxStyler)
                .build();
        DefaultGpxRunner defaultGpxRunner = new DefaultGpxRunner(gpxMapper);
        Path gpx = resourceDirectory.resolve("test.gpx");
        defaultGpxRunner.run(gpx.toFile(), tempDir);

        // The image should be properly generated
        // Then it should be drawn according to the style
        BufferedImage resultingImage = ImageIO.read(tempDir.resolve("test.png").toFile());
        assertEquals(900, resultingImage.getHeight());
        assertEquals(1200, resultingImage.getWidth());
    }
}
