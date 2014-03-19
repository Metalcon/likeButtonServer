package de.metalcon.like.api;

import static org.junit.Assert.fail;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class AbstractLikeServiceTest {

    protected final static String TEST_FOLDER = "/dev/shm/like/";

    public static LikeService likeService;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        File testDirectory = new File(TEST_FOLDER);
        if (!testDirectory.exists()) {
            boolean success = testDirectory.mkdirs();
            if (!success) {
                System.out.println("Directory creation failed");
            }
        }
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        likeService.clear();
        try {
            likeService = new LikeService(TEST_FOLDER);
        } catch (Exception e) {
            e.printStackTrace();
            fail("cannot reset data base for new tests");
        }
    }

    @After
    public void tearDown() throws Exception {

    }

    //    @Test
    //    public void testDeleteEdge() {
    //        fail("Not yet implemented");
    //    }
    //
    //    @Test
    //    public void testGetLikedInNodes() {
    //        fail("Not yet implemented");
    //    }
    //
    //    @Test
    //    public void testGetLikedOutNodes() {
    //        fail("Not yet implemented");
    //    }
    //
    //    @Test
    //    public void testGetDislikedInNodes() {
    //        fail("Not yet implemented");
    //    }
    //
    //    @Test
    //    public void testGetDislikedOutNodes() {
    //        fail("Not yet implemented");
    //    }
    //
    //    @Test
    //    public void testGetLikedLikes() {
    //        fail("Not yet implemented");
    //    }

}
