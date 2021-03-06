package com.espressoprogrammer.library.service;

import com.espressoprogrammer.library.dto.Book;
import com.espressoprogrammer.library.dto.DateReadingSession;
import com.espressoprogrammer.library.dto.ReadingSession;
import com.espressoprogrammer.library.dto.ReadingSessionProgress;
import com.espressoprogrammer.library.persistence.BooksDao;
import com.espressoprogrammer.library.persistence.ReadingSessionsDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.espressoprogrammer.library.util.LibraryTestUtil.getTestBook;
import static com.espressoprogrammer.library.util.LibraryTestUtil.getTestDateReadingSession;
import static com.espressoprogrammer.library.util.LibraryTestUtil.getTestReadingSession;
import static com.espressoprogrammer.library.util.LibraryTestUtil.getTestReadingSessionProgress;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReadingSessionsServiceTest {
    private static final String JOHN_DOE_USER = "johndoe";
    private static final String BOOK_UUID = "1e4014b1-a551-4310-9f30-590c3140b695";
    private static final String READING_SESSION_UUID = "1e4014b1-a551-4310-9f30-590c3140b695";

    @MockBean
    private BooksDao booksDao;

    @MockBean
    private ReadingSessionsDao readingSessionsDao;

    @Autowired
    private ReadingSessionsService readingSessionsService;

    @Test
    public void getUserReadingSessions() throws Exception {
        List<ReadingSession> readingSessions = new ArrayList<>();
        readingSessions.add(getTestReadingSession(READING_SESSION_UUID + ".json"));
        when(readingSessionsDao.getUserReadingSessions(JOHN_DOE_USER, BOOK_UUID)).thenReturn(readingSessions);

        List<ReadingSession> actualReadingSessions = readingSessionsService.getUserReadingSessions(JOHN_DOE_USER, BOOK_UUID);
        assertThat(actualReadingSessions).isEqualTo(readingSessions);
    }

    @Test
    public void getUserCurrentReadingSession() throws Exception {
        Book book = getTestBook(BOOK_UUID + ".json");
        when(booksDao.getUserBook(JOHN_DOE_USER, BOOK_UUID)).thenReturn(Optional.of(book));

        ArrayList<ReadingSession> readingSessions = new ArrayList<>();
        readingSessions.add(getTestReadingSession(READING_SESSION_UUID + ".json"));
        when(readingSessionsDao.getUserReadingSessions(JOHN_DOE_USER, BOOK_UUID)).thenReturn(readingSessions);

        List<ReadingSession> actualReadingSessions = readingSessionsService.getUserReadingSessions(JOHN_DOE_USER, BOOK_UUID);
        assertThat(actualReadingSessions).isEqualTo(readingSessions);
    }

    @Test
    public void getMissingUserCurrentReadingSession() throws Exception {
        when(booksDao.getUserBook(JOHN_DOE_USER, BOOK_UUID)).thenReturn(Optional.empty());

        try {
            readingSessionsService.getUserCurrentReadingSession(JOHN_DOE_USER, BOOK_UUID);
            fail("It should fail with " + BooksException.Reason.BOOK_NOT_FOUND);
        } catch(BooksException ex) {
            assertThat(ex.getReason()).isEqualTo(BooksException.Reason.BOOK_NOT_FOUND);
        } catch (Exception ex) {
            fail("It should fail with " + BooksException.Reason.BOOK_NOT_FOUND);
        }
    }

    @Test
    public void createUserReadingSession() throws Exception {
        Book book = getTestBook(BOOK_UUID + ".json");
        when(booksDao.getUserBook(JOHN_DOE_USER, BOOK_UUID)).thenReturn(Optional.of(book));

        ReadingSession readingSession = getTestReadingSession(READING_SESSION_UUID + "-request.json");
        ReadingSession actualReadingSessionRequest = readingSession.copy(
                readingSession.getUuid(),
                readingSession.getBookUuid(),
                readingSession.getDeadline(),
                Collections.emptyList());
        when(readingSessionsDao.createUserReadingSession(JOHN_DOE_USER, BOOK_UUID, actualReadingSessionRequest))
                .thenReturn(getTestReadingSession(READING_SESSION_UUID + ".json"));

        ReadingSession actualReadingSession = readingSessionsService.createUserReadingSession(JOHN_DOE_USER, BOOK_UUID, readingSession);
        assertThat(actualReadingSession).isEqualTo(getTestReadingSession(READING_SESSION_UUID + ".json"));
    }

    @Test
    public void createAdditionalUserReadingSession() throws Exception {
        Book book = getTestBook(BOOK_UUID + ".json");
        when(booksDao.getUserBook(JOHN_DOE_USER, BOOK_UUID)).thenReturn(Optional.of(book));

        when(readingSessionsDao.getUserReadingSessions(JOHN_DOE_USER, BOOK_UUID)).thenReturn(Arrays.asList(getTestReadingSession(READING_SESSION_UUID + ".json")));

        try {
            readingSessionsService.createUserReadingSession(JOHN_DOE_USER, BOOK_UUID, getTestReadingSession(READING_SESSION_UUID + "-request.json"));
            fail("It should fail with " + ReadingSessionsException.Reason.READING_SESSION_ALREADY_EXISTS);
        } catch(ReadingSessionsException ex) {
            assertThat(ex.getReason()).isEqualTo(ReadingSessionsException.Reason.READING_SESSION_ALREADY_EXISTS);
        } catch (Exception ex) {
            fail("It should fail with " + ReadingSessionsException.Reason.READING_SESSION_ALREADY_EXISTS);
        }
    }

    @Test
    public void getUserReadingSession() throws Exception {
        ReadingSession readingSession = getTestReadingSession(READING_SESSION_UUID + ".json");
        when(readingSessionsDao.getUserReadingSession(JOHN_DOE_USER, BOOK_UUID, READING_SESSION_UUID)).thenReturn(Optional.of(readingSession));

        ReadingSession actualReadingSession = readingSessionsService.getUserReadingSession(JOHN_DOE_USER, BOOK_UUID, READING_SESSION_UUID);
        assertThat(actualReadingSession).isEqualTo(readingSession);
    }

    @Test
    public void getMissingUserReadingSession() throws Exception {
        String uuid = "missing-uuid-1";
        when(readingSessionsDao.getUserReadingSession(JOHN_DOE_USER, BOOK_UUID, uuid)).thenReturn(Optional.empty());

        try {
            readingSessionsService.getUserReadingSession(JOHN_DOE_USER, BOOK_UUID, uuid);
            fail("It should fail with " + ReadingSessionsException.Reason.READING_SESSION_NOT_FOUND);
        } catch(ReadingSessionsException ex) {
            assertThat(ex.getReason()).isEqualTo(ReadingSessionsException.Reason.READING_SESSION_NOT_FOUND);
        } catch (Exception ex) {
            fail("It should fail with " + ReadingSessionsException.Reason.READING_SESSION_NOT_FOUND);
        }
    }

    @Test
    public void deleteUserReadingSession() throws Exception {
        ReadingSession readingSession = getTestReadingSession(READING_SESSION_UUID + ".json");
        when(readingSessionsDao.deleteUserReadingSession(JOHN_DOE_USER, BOOK_UUID, readingSession.getUuid())).thenReturn(Optional.of(readingSession.getUuid()));

        String actualUuid = readingSessionsService.deleteUserReadingSession(JOHN_DOE_USER, BOOK_UUID, readingSession.getUuid());
        assertThat(actualUuid).isEqualTo(readingSession.getUuid());
    }

    @Test
    public void deleteMissingUserReadingSession() throws Exception {
        ReadingSession readingSession = getTestReadingSession(READING_SESSION_UUID + ".json");
        when(readingSessionsDao.deleteUserReadingSession(JOHN_DOE_USER, BOOK_UUID, readingSession.getUuid())).thenReturn(Optional.empty());

        try {
            readingSessionsService.deleteUserReadingSession(JOHN_DOE_USER, BOOK_UUID, readingSession.getUuid());
            fail("It should fail with " + ReadingSessionsException.Reason.READING_SESSION_NOT_FOUND);
        } catch(ReadingSessionsException ex) {
            assertThat(ex.getReason()).isEqualTo(ReadingSessionsException.Reason.READING_SESSION_NOT_FOUND);
        } catch (Exception ex) {
            fail("It should fail with " + ReadingSessionsException.Reason.READING_SESSION_NOT_FOUND);
        }
    }

    @Test
    public void createDateReadingSession() throws Exception {
        ReadingSession readingSession = getTestReadingSession(READING_SESSION_UUID + ".json");
        when(readingSessionsDao.getUserReadingSession(JOHN_DOE_USER, BOOK_UUID,READING_SESSION_UUID)).thenReturn(Optional.of(readingSession));

        DateReadingSession dateReadingSession = getTestDateReadingSession(READING_SESSION_UUID + "-new-date-reading-session.json");
        DateReadingSession actualReadingSession = readingSessionsService.createDateReadingSession(JOHN_DOE_USER, BOOK_UUID, READING_SESSION_UUID, dateReadingSession);
        assertThat(actualReadingSession).isEqualTo(dateReadingSession);
    }

    @Test
    public void createDateReadingSessionNullDate() throws Exception {
        ReadingSession readingSession = getTestReadingSession(READING_SESSION_UUID + ".json");
        when(readingSessionsDao.getUserReadingSession(JOHN_DOE_USER, BOOK_UUID,READING_SESSION_UUID)).thenReturn(Optional.of(readingSession));

        DateReadingSession dateReadingSession = getTestDateReadingSession(READING_SESSION_UUID + "-new-date-reading-session.json");
        DateReadingSession invalidDateReadingSession = dateReadingSession.copy(null,
                dateReadingSession.getLastReadPage(),
                dateReadingSession.getBookmark());
        try {
            readingSessionsService.createDateReadingSession(JOHN_DOE_USER, BOOK_UUID, READING_SESSION_UUID, invalidDateReadingSession);
            fail("It should fail with " + ReadingSessionsException.Reason.DATE_READING_SESSION_INVALID);
        } catch(ReadingSessionsException ex) {
            assertThat(ex.getReason()).isEqualTo(ReadingSessionsException.Reason.DATE_READING_SESSION_INVALID);
        } catch (Exception ex) {
            fail("It should fail with " + ReadingSessionsException.Reason.DATE_READING_SESSION_INVALID);
        }
    }

    @Test
    public void createDateReadingSessionInvalidDate() throws Exception {
        ReadingSession readingSession = getTestReadingSession(READING_SESSION_UUID + ".json");
        when(readingSessionsDao.getUserReadingSession(JOHN_DOE_USER, BOOK_UUID,READING_SESSION_UUID)).thenReturn(Optional.of(readingSession));

        DateReadingSession dateReadingSession = getTestDateReadingSession(READING_SESSION_UUID + "-new-date-reading-session.json");
        DateReadingSession invalidDateReadingSession = dateReadingSession.copy("2019-99-99",
                dateReadingSession.getLastReadPage(),
                dateReadingSession.getBookmark());
        try {
            readingSessionsService.createDateReadingSession(JOHN_DOE_USER, BOOK_UUID, READING_SESSION_UUID, invalidDateReadingSession);
            fail("It should fail with " + ReadingSessionsException.Reason.DATE_READING_SESSION_INVALID);
        } catch(ReadingSessionsException ex) {
            assertThat(ex.getReason()).isEqualTo(ReadingSessionsException.Reason.DATE_READING_SESSION_INVALID);
        } catch (Exception ex) {
            fail("It should fail with " + ReadingSessionsException.Reason.DATE_READING_SESSION_INVALID);
        }
    }

    @Test
    public void createDateReadingSessionNullLastReadPage() throws Exception {
        ReadingSession readingSession = getTestReadingSession(READING_SESSION_UUID + ".json");
        when(readingSessionsDao.getUserReadingSession(JOHN_DOE_USER, BOOK_UUID,READING_SESSION_UUID)).thenReturn(Optional.of(readingSession));

        DateReadingSession dateReadingSession = getTestDateReadingSession(READING_SESSION_UUID + "-new-date-reading-session.json");
        DateReadingSession invalidDateReadingSession = dateReadingSession.copy(dateReadingSession.getDate(),
                null,
                dateReadingSession.getBookmark());
        try {
            readingSessionsService.createDateReadingSession(JOHN_DOE_USER, BOOK_UUID, READING_SESSION_UUID, invalidDateReadingSession);
            fail("It should fail with " + ReadingSessionsException.Reason.DATE_READING_SESSION_INVALID);
        } catch(ReadingSessionsException ex) {
            assertThat(ex.getReason()).isEqualTo(ReadingSessionsException.Reason.DATE_READING_SESSION_INVALID);
        } catch (Exception ex) {
            fail("It should fail with " + ReadingSessionsException.Reason.DATE_READING_SESSION_INVALID);
        }
    }

    @Test
    public void createDateReadingSessionZeroLastReadPage() throws Exception {
        ReadingSession readingSession = getTestReadingSession(READING_SESSION_UUID + ".json");
        when(readingSessionsDao.getUserReadingSession(JOHN_DOE_USER, BOOK_UUID,READING_SESSION_UUID)).thenReturn(Optional.of(readingSession));

        DateReadingSession dateReadingSession = getTestDateReadingSession(READING_SESSION_UUID + "-new-date-reading-session.json");
        DateReadingSession invalidDateReadingSession = dateReadingSession.copy(dateReadingSession.getDate(),
                0,
                dateReadingSession.getBookmark());
        try {
            readingSessionsService.createDateReadingSession(JOHN_DOE_USER, BOOK_UUID, READING_SESSION_UUID, invalidDateReadingSession);
            fail("It should fail with " + ReadingSessionsException.Reason.DATE_READING_SESSION_INVALID);
        } catch(ReadingSessionsException ex) {
            assertThat(ex.getReason()).isEqualTo(ReadingSessionsException.Reason.DATE_READING_SESSION_INVALID);
        } catch (Exception ex) {
            fail("It should fail with " + ReadingSessionsException.Reason.DATE_READING_SESSION_INVALID);
        }
    }

    @Test
    public void createDateReadingSessionExistingDate() throws Exception {
        ReadingSession readingSession = getTestReadingSession(READING_SESSION_UUID + ".json");
        when(readingSessionsDao.getUserReadingSession(JOHN_DOE_USER,
            BOOK_UUID, READING_SESSION_UUID)).thenReturn(Optional.of(readingSession));

        try {
            DateReadingSession dateReadingSession = getTestDateReadingSession(READING_SESSION_UUID + "-existing-date-reading-session.json");
            readingSessionsService.createDateReadingSession(JOHN_DOE_USER, BOOK_UUID, READING_SESSION_UUID, dateReadingSession);
            fail("It should fail with " + ReadingSessionsException.Reason.DATE_READING_SESSION_ALREADY_EXISTS);
        } catch(ReadingSessionsException ex) {
            assertThat(ex.getReason()).isEqualTo(ReadingSessionsException.Reason.DATE_READING_SESSION_ALREADY_EXISTS);
        } catch (Exception ex) {
            fail("It should fail with " + ReadingSessionsException.Reason.DATE_READING_SESSION_ALREADY_EXISTS);
        }
    }

    @Test
    public void getDateReadingSession() throws Exception {
        ReadingSession readingSession = getTestReadingSession(READING_SESSION_UUID + ".json");
        when(readingSessionsDao.getUserReadingSession(JOHN_DOE_USER, BOOK_UUID, READING_SESSION_UUID)).thenReturn(Optional.of(readingSession));
        String date = "2017-01-01";

        DateReadingSession actualDateReadingSession = readingSessionsService.getDateReadingSession(JOHN_DOE_USER, BOOK_UUID, READING_SESSION_UUID, date);
        assertThat(actualDateReadingSession).isEqualTo(readingSession.getDateReadingSessions().get(0));
    }

    @Test
    public void getMissingDateReadingSession() throws Exception {
        ReadingSession readingSession = getTestReadingSession(READING_SESSION_UUID + ".json");
        when(readingSessionsDao.getUserReadingSession(JOHN_DOE_USER, BOOK_UUID, READING_SESSION_UUID)).thenReturn(Optional.of(readingSession));
        String date = "2017-01-02";

        try {
            readingSessionsService.getDateReadingSession(JOHN_DOE_USER, BOOK_UUID, READING_SESSION_UUID, date);
            fail("It should fail with " + ReadingSessionsException.Reason.DATE_READING_SESSION_NOT_FOUND);
        } catch(ReadingSessionsException ex) {
            assertThat(ex.getReason()).isEqualTo(ReadingSessionsException.Reason.DATE_READING_SESSION_NOT_FOUND);
        } catch (Exception ex) {
            fail("It should fail with " + ReadingSessionsException.Reason.DATE_READING_SESSION_NOT_FOUND);
        }
    }

    @Test
    public void updateDateReadingSession() throws Exception {
        ReadingSession readingSession = getTestReadingSession(READING_SESSION_UUID + ".json");
        when(readingSessionsDao.getUserReadingSession(JOHN_DOE_USER, BOOK_UUID, READING_SESSION_UUID)).thenReturn(Optional.of(readingSession));

        String date = "2017-01-01";
        DateReadingSession dateReadingSession = getTestDateReadingSession(READING_SESSION_UUID + "-update-date-reading-session-request.json");
        String actualDate = readingSessionsService.updateDateReadingSession(JOHN_DOE_USER, BOOK_UUID, READING_SESSION_UUID, date, dateReadingSession);
        assertThat(actualDate).isEqualTo(date);
    }

    @Test
    public void updateDateReadingSessionNullDate() throws Exception {
        ReadingSession readingSession = getTestReadingSession(READING_SESSION_UUID + ".json");
        when(readingSessionsDao.getUserReadingSession(JOHN_DOE_USER, BOOK_UUID,READING_SESSION_UUID)).thenReturn(Optional.of(readingSession));

        String date = "2017-01-01";
        DateReadingSession dateReadingSession = getTestDateReadingSession(READING_SESSION_UUID + "-update-date-reading-session-request.json");
        DateReadingSession invalidDateReadingSession = dateReadingSession.copy(null,
                dateReadingSession.getLastReadPage(),
                dateReadingSession.getBookmark());
        try {
            readingSessionsService.updateDateReadingSession(JOHN_DOE_USER, BOOK_UUID, READING_SESSION_UUID, date, invalidDateReadingSession);
            fail("It should fail with " + ReadingSessionsException.Reason.DATE_READING_SESSION_INVALID);
        } catch(ReadingSessionsException ex) {
            assertThat(ex.getReason()).isEqualTo(ReadingSessionsException.Reason.DATE_READING_SESSION_INVALID);
        } catch (Exception ex) {
            fail("It should fail with " + ReadingSessionsException.Reason.DATE_READING_SESSION_INVALID);
        }
    }

    @Test
    public void updateDateReadingSessionInvalidDate() throws Exception {
        ReadingSession readingSession = getTestReadingSession(READING_SESSION_UUID + ".json");
        when(readingSessionsDao.getUserReadingSession(JOHN_DOE_USER, BOOK_UUID,READING_SESSION_UUID)).thenReturn(Optional.of(readingSession));

        String date = "2017-01-01";
        DateReadingSession dateReadingSession = getTestDateReadingSession(READING_SESSION_UUID + "-update-date-reading-session-request.json");
        DateReadingSession invalidDateReadingSession = dateReadingSession.copy("2019-99-99",
                dateReadingSession.getLastReadPage(),
                dateReadingSession.getBookmark());
        try {
            readingSessionsService.updateDateReadingSession(JOHN_DOE_USER, BOOK_UUID, READING_SESSION_UUID, date, invalidDateReadingSession);
            fail("It should fail with " + ReadingSessionsException.Reason.DATE_READING_SESSION_INVALID);
        } catch(ReadingSessionsException ex) {
            assertThat(ex.getReason()).isEqualTo(ReadingSessionsException.Reason.DATE_READING_SESSION_INVALID);
        } catch (Exception ex) {
            fail("It should fail with " + ReadingSessionsException.Reason.DATE_READING_SESSION_INVALID);
        }
    }

    @Test
    public void updateDateReadingSessionNullLastReadPage() throws Exception {
        ReadingSession readingSession = getTestReadingSession(READING_SESSION_UUID + ".json");
        when(readingSessionsDao.getUserReadingSession(JOHN_DOE_USER, BOOK_UUID,READING_SESSION_UUID)).thenReturn(Optional.of(readingSession));

        String date = "2017-01-01";
        DateReadingSession dateReadingSession = getTestDateReadingSession(READING_SESSION_UUID + "-update-date-reading-session-request.json");
        DateReadingSession invalidDateReadingSession = dateReadingSession.copy(dateReadingSession.getDate(),
                null,
                dateReadingSession.getBookmark());
        try {
            readingSessionsService.updateDateReadingSession(JOHN_DOE_USER, BOOK_UUID, READING_SESSION_UUID, date, invalidDateReadingSession);
            fail("It should fail with " + ReadingSessionsException.Reason.DATE_READING_SESSION_INVALID);
        } catch(ReadingSessionsException ex) {
            assertThat(ex.getReason()).isEqualTo(ReadingSessionsException.Reason.DATE_READING_SESSION_INVALID);
        } catch (Exception ex) {
            fail("It should fail with " + ReadingSessionsException.Reason.DATE_READING_SESSION_INVALID);
        }
    }

    @Test
    public void updateDateReadingSessionZeroLastReadPage() throws Exception {
        ReadingSession readingSession = getTestReadingSession(READING_SESSION_UUID + ".json");
        when(readingSessionsDao.getUserReadingSession(JOHN_DOE_USER, BOOK_UUID,READING_SESSION_UUID)).thenReturn(Optional.of(readingSession));

        String date = "2017-01-01";
        DateReadingSession dateReadingSession = getTestDateReadingSession(READING_SESSION_UUID + "-update-date-reading-session-request.json");
        DateReadingSession invalidDateReadingSession = dateReadingSession.copy(dateReadingSession.getDate(),
                0,
                dateReadingSession.getBookmark());
        try {
            readingSessionsService.updateDateReadingSession(JOHN_DOE_USER, BOOK_UUID, READING_SESSION_UUID, date, invalidDateReadingSession);
            fail("It should fail with " + ReadingSessionsException.Reason.DATE_READING_SESSION_INVALID);
        } catch(ReadingSessionsException ex) {
            assertThat(ex.getReason()).isEqualTo(ReadingSessionsException.Reason.DATE_READING_SESSION_INVALID);
        } catch (Exception ex) {
            fail("It should fail with " + ReadingSessionsException.Reason.DATE_READING_SESSION_INVALID);
        }
    }

    @Test
    public void updateMissingDateReadingSession() throws Exception {
        ReadingSession readingSession = getTestReadingSession(READING_SESSION_UUID + ".json");
        when(readingSessionsDao.getUserReadingSession(JOHN_DOE_USER,
            BOOK_UUID, READING_SESSION_UUID)).thenReturn(Optional.of(readingSession));

        String date = "2017-01-02";
        try {
            DateReadingSession dateReadingSession = getTestDateReadingSession(READING_SESSION_UUID + "-update-date-reading-session-request.json");
            readingSessionsService.updateDateReadingSession(JOHN_DOE_USER, BOOK_UUID, READING_SESSION_UUID, date, dateReadingSession);
            fail("It should fail with " + ReadingSessionsException.Reason.DATE_READING_SESSION_NOT_FOUND);
        } catch(ReadingSessionsException ex) {
            assertThat(ex.getReason()).isEqualTo(ReadingSessionsException.Reason.DATE_READING_SESSION_NOT_FOUND);
        } catch (Exception ex) {
            fail("It should fail with " + ReadingSessionsException.Reason.DATE_READING_SESSION_NOT_FOUND);
        }
    }

    @Test
    public void deleteDateReadingSession() throws Exception {
        ReadingSession readingSession = getTestReadingSession(READING_SESSION_UUID + ".json");
        when(readingSessionsDao.getUserReadingSession(JOHN_DOE_USER,
            BOOK_UUID, READING_SESSION_UUID)).thenReturn(Optional.of(readingSession));

        String date = "2017-01-01";
        String actualDate = readingSessionsService.deleteDateReadingSession(JOHN_DOE_USER, BOOK_UUID, READING_SESSION_UUID, date);
        assertThat(actualDate).isEqualTo(date);
    }

    @Test
    public void deleteMissingDateReadingSession() throws Exception {
        ReadingSession readingSession = getTestReadingSession(READING_SESSION_UUID + ".json");
        when(readingSessionsDao.getUserReadingSession(JOHN_DOE_USER,
            BOOK_UUID, READING_SESSION_UUID)).thenReturn(Optional.of(readingSession));

        String date = "2017-01-02";
        try {
            readingSessionsService.deleteDateReadingSession(JOHN_DOE_USER, BOOK_UUID, READING_SESSION_UUID, date);
            fail("It should fail with " + ReadingSessionsException.Reason.DATE_READING_SESSION_NOT_FOUND);
        } catch(ReadingSessionsException ex) {
            assertThat(ex.getReason()).isEqualTo(ReadingSessionsException.Reason.DATE_READING_SESSION_NOT_FOUND);
        } catch (Exception ex) {
            fail("It should fail with " + ReadingSessionsException.Reason.DATE_READING_SESSION_NOT_FOUND);
        }
    }

    @Test
    public void getUserReadingSessionProgressWithOneReading() throws Exception {
        Book book = getTestBook(BOOK_UUID + ".json");
        when(booksDao.getUserBook(JOHN_DOE_USER, BOOK_UUID)).thenReturn(Optional.of(book));

        ReadingSession readingSession = getTestReadingSession(READING_SESSION_UUID + "-one-reading.json");
        when(readingSessionsDao.getUserReadingSession(JOHN_DOE_USER, BOOK_UUID, READING_SESSION_UUID)).thenReturn(Optional.of(readingSession));

        ReadingSessionProgress actualReadingSessionProgress = readingSessionsService.getUserReadingSessionProgress(JOHN_DOE_USER, BOOK_UUID, READING_SESSION_UUID);
        ReadingSessionProgress expectedReadingSessionProgressTemplate = getTestReadingSessionProgress(READING_SESSION_UUID + "-one-reading-progress.json");
        ReadingSessionProgress expectedReadingSessionProgress = expectedReadingSessionProgressTemplate.copy(expectedReadingSessionProgressTemplate.getBookUuid(),
                expectedReadingSessionProgressTemplate.getLastReadPage(),
                expectedReadingSessionProgressTemplate.getPagesTotal(),
                expectedReadingSessionProgressTemplate.getReadPercentage(),
                expectedReadingSessionProgressTemplate.getAveragePagesPerDay(),
                expectedReadingSessionProgressTemplate.getEstimatedReadDaysLeft(),
                expectedReadingSessionProgressTemplate.getEstimatedDaysLeft(),
                LocalDate.now().plusDays(expectedReadingSessionProgressTemplate.getEstimatedDaysLeft().intValue()).toString(),
                expectedReadingSessionProgressTemplate.getDeadline());

        assertThat(actualReadingSessionProgress.getLastReadPage()).isEqualTo(expectedReadingSessionProgress.getLastReadPage());
        assertThat(actualReadingSessionProgress.getPagesTotal()).isEqualTo(expectedReadingSessionProgress.getPagesTotal());
        assertThat(actualReadingSessionProgress.getReadPercentage()).isEqualTo(expectedReadingSessionProgress.getReadPercentage());
        assertThat(actualReadingSessionProgress.getAveragePagesPerDay()).isEqualTo(expectedReadingSessionProgress.getAveragePagesPerDay());
        assertThat(actualReadingSessionProgress.getEstimatedReadDaysLeft()).isEqualTo(expectedReadingSessionProgress.getEstimatedReadDaysLeft());
        assertThat(actualReadingSessionProgress.getEstimatedDaysLeft()).isGreaterThanOrEqualTo(expectedReadingSessionProgress.getEstimatedDaysLeft());
        assertThat(actualReadingSessionProgress.getEstimatedFinishDate()).isGreaterThanOrEqualTo(expectedReadingSessionProgress.getEstimatedFinishDate());
    }

    @Test
    public void getUserReadingSessionProgressWithTwoConsecutiveReadings() throws Exception {
        Book book = getTestBook(BOOK_UUID + ".json");
        when(booksDao.getUserBook(JOHN_DOE_USER, BOOK_UUID)).thenReturn(Optional.of(book));

        ReadingSession readingSession = getTestReadingSession(READING_SESSION_UUID + "-two-consecutive-readings.json");
        when(readingSessionsDao.getUserReadingSession(JOHN_DOE_USER, BOOK_UUID, READING_SESSION_UUID)).thenReturn(Optional.of(readingSession));

        ReadingSessionProgress actualReadingSessionProgress = readingSessionsService.getUserReadingSessionProgress(JOHN_DOE_USER, BOOK_UUID, READING_SESSION_UUID);
        ReadingSessionProgress expectedReadingSessionProgressTemplate = getTestReadingSessionProgress(READING_SESSION_UUID + "-two-consecutive-readings-progress.json");
        ReadingSessionProgress expectedReadingSessionProgress = expectedReadingSessionProgressTemplate.copy(expectedReadingSessionProgressTemplate.getBookUuid(),
                expectedReadingSessionProgressTemplate.getLastReadPage(),
                expectedReadingSessionProgressTemplate.getPagesTotal(),
                expectedReadingSessionProgressTemplate.getReadPercentage(),
                expectedReadingSessionProgressTemplate.getAveragePagesPerDay(),
                expectedReadingSessionProgressTemplate.getEstimatedReadDaysLeft(),
                expectedReadingSessionProgressTemplate.getEstimatedDaysLeft(),
                LocalDate.now().plusDays(expectedReadingSessionProgressTemplate.getEstimatedDaysLeft().intValue()).toString(),
                expectedReadingSessionProgressTemplate.getDeadline());

        assertThat(actualReadingSessionProgress.getLastReadPage()).isEqualTo(expectedReadingSessionProgress.getLastReadPage());
        assertThat(actualReadingSessionProgress.getPagesTotal()).isEqualTo(expectedReadingSessionProgress.getPagesTotal());
        assertThat(actualReadingSessionProgress.getReadPercentage()).isEqualTo(expectedReadingSessionProgress.getReadPercentage());
        assertThat(actualReadingSessionProgress.getAveragePagesPerDay()).isEqualTo(expectedReadingSessionProgress.getAveragePagesPerDay());
        assertThat(actualReadingSessionProgress.getEstimatedReadDaysLeft()).isEqualTo(expectedReadingSessionProgress.getEstimatedReadDaysLeft());
        assertThat(actualReadingSessionProgress.getEstimatedDaysLeft()).isGreaterThanOrEqualTo(expectedReadingSessionProgress.getEstimatedDaysLeft());
        assertThat(actualReadingSessionProgress.getEstimatedFinishDate()).isGreaterThanOrEqualTo(expectedReadingSessionProgress.getEstimatedFinishDate());
    }

    @Test
    public void getUserReadingSessionProgressWithGapConsecutiveReadings() throws Exception {
        Book book = getTestBook(BOOK_UUID + ".json");
        when(booksDao.getUserBook(JOHN_DOE_USER, BOOK_UUID)).thenReturn(Optional.of(book));

        ReadingSession readingSession = getTestReadingSession(READING_SESSION_UUID + "-two-gap-readings.json");
        when(readingSessionsDao.getUserReadingSession(JOHN_DOE_USER, BOOK_UUID, READING_SESSION_UUID)).thenReturn(Optional.of(readingSession));

        ReadingSessionProgress actualReadingSessionProgress = readingSessionsService.getUserReadingSessionProgress(JOHN_DOE_USER, BOOK_UUID, READING_SESSION_UUID);
        ReadingSessionProgress expectedReadingSessionProgressTemplate = getTestReadingSessionProgress(READING_SESSION_UUID + "-two-gap-readings-progress.json");
        ReadingSessionProgress expectedReadingSessionProgress = expectedReadingSessionProgressTemplate.copy(expectedReadingSessionProgressTemplate.getBookUuid(),
                expectedReadingSessionProgressTemplate.getLastReadPage(),
                expectedReadingSessionProgressTemplate.getPagesTotal(),
                expectedReadingSessionProgressTemplate.getReadPercentage(),
                expectedReadingSessionProgressTemplate.getAveragePagesPerDay(),
                expectedReadingSessionProgressTemplate.getEstimatedReadDaysLeft(),
                expectedReadingSessionProgressTemplate.getEstimatedDaysLeft(),
                LocalDate.now().plusDays(expectedReadingSessionProgressTemplate.getEstimatedDaysLeft().intValue()).toString(),
                expectedReadingSessionProgressTemplate.getDeadline());

        assertThat(actualReadingSessionProgress.getLastReadPage()).isEqualTo(expectedReadingSessionProgress.getLastReadPage());
        assertThat(actualReadingSessionProgress.getPagesTotal()).isEqualTo(expectedReadingSessionProgress.getPagesTotal());
        assertThat(actualReadingSessionProgress.getReadPercentage()).isEqualTo(expectedReadingSessionProgress.getReadPercentage());
        assertThat(actualReadingSessionProgress.getAveragePagesPerDay()).isEqualTo(expectedReadingSessionProgress.getAveragePagesPerDay());
        assertThat(actualReadingSessionProgress.getEstimatedReadDaysLeft()).isEqualTo(expectedReadingSessionProgress.getEstimatedReadDaysLeft());
        assertThat(actualReadingSessionProgress.getEstimatedDaysLeft()).isGreaterThanOrEqualTo(expectedReadingSessionProgress.getEstimatedDaysLeft());
        assertThat(actualReadingSessionProgress.getEstimatedFinishDate()).isGreaterThanOrEqualTo(expectedReadingSessionProgress.getEstimatedFinishDate());
    }

    @Test
    public void getUserReadingSessionProgressWithThreeConsecutiveReadings() throws Exception {
        Book book = getTestBook(BOOK_UUID + ".json");
        when(booksDao.getUserBook(JOHN_DOE_USER, BOOK_UUID)).thenReturn(Optional.of(book));

        ReadingSession readingSession = getTestReadingSession(READING_SESSION_UUID + "-three-consecutive-readings.json");
        when(readingSessionsDao.getUserReadingSession(JOHN_DOE_USER, BOOK_UUID, READING_SESSION_UUID)).thenReturn(Optional.of(readingSession));

        ReadingSessionProgress actualReadingSessionProgress = readingSessionsService.getUserReadingSessionProgress(JOHN_DOE_USER, BOOK_UUID, READING_SESSION_UUID);
        ReadingSessionProgress expectedReadingSessionProgressTemplate = getTestReadingSessionProgress(READING_SESSION_UUID + "-three-consecutive-readings-progress.json");
        ReadingSessionProgress expectedReadingSessionProgress = expectedReadingSessionProgressTemplate.copy(expectedReadingSessionProgressTemplate.getBookUuid(),
                expectedReadingSessionProgressTemplate.getLastReadPage(),
                expectedReadingSessionProgressTemplate.getPagesTotal(),
                expectedReadingSessionProgressTemplate.getReadPercentage(),
                expectedReadingSessionProgressTemplate.getAveragePagesPerDay(),
                expectedReadingSessionProgressTemplate.getEstimatedReadDaysLeft(),
                expectedReadingSessionProgressTemplate.getEstimatedDaysLeft(),
                LocalDate.now().plusDays(expectedReadingSessionProgressTemplate.getEstimatedDaysLeft().intValue()).toString(),
                expectedReadingSessionProgressTemplate.getDeadline());

        assertThat(actualReadingSessionProgress.getLastReadPage()).isEqualTo(expectedReadingSessionProgress.getLastReadPage());
        assertThat(actualReadingSessionProgress.getPagesTotal()).isEqualTo(expectedReadingSessionProgress.getPagesTotal());
        assertThat(actualReadingSessionProgress.getReadPercentage()).isEqualTo(expectedReadingSessionProgress.getReadPercentage());
        assertThat(actualReadingSessionProgress.getAveragePagesPerDay()).isEqualTo(expectedReadingSessionProgress.getAveragePagesPerDay());
        assertThat(actualReadingSessionProgress.getEstimatedReadDaysLeft()).isEqualTo(expectedReadingSessionProgress.getEstimatedReadDaysLeft());
        assertThat(actualReadingSessionProgress.getEstimatedDaysLeft()).isGreaterThanOrEqualTo(expectedReadingSessionProgress.getEstimatedDaysLeft());
        assertThat(actualReadingSessionProgress.getEstimatedFinishDate()).isGreaterThanOrEqualTo(expectedReadingSessionProgress.getEstimatedFinishDate());
    }

    @Test
    public void getUserReadingSessionProgressWithThreeGapReadings() throws Exception {
        Book book = getTestBook(BOOK_UUID + ".json");
        when(booksDao.getUserBook(JOHN_DOE_USER, BOOK_UUID)).thenReturn(Optional.of(book));

        ReadingSession readingSession = getTestReadingSession(READING_SESSION_UUID + "-three-gap-readings.json");
        when(readingSessionsDao.getUserReadingSession(JOHN_DOE_USER, BOOK_UUID, READING_SESSION_UUID)).thenReturn(Optional.of(readingSession));

        ReadingSessionProgress actualReadingSessionProgress = readingSessionsService.getUserReadingSessionProgress(JOHN_DOE_USER, BOOK_UUID, READING_SESSION_UUID);
        ReadingSessionProgress expectedReadingSessionProgressTemplate = getTestReadingSessionProgress(READING_SESSION_UUID + "-three-gap-readings-progress.json");
        ReadingSessionProgress expectedReadingSessionProgress = expectedReadingSessionProgressTemplate.copy(expectedReadingSessionProgressTemplate.getBookUuid(),
                expectedReadingSessionProgressTemplate.getLastReadPage(),
                expectedReadingSessionProgressTemplate.getPagesTotal(),
                expectedReadingSessionProgressTemplate.getReadPercentage(),
                expectedReadingSessionProgressTemplate.getAveragePagesPerDay(),
                expectedReadingSessionProgressTemplate.getEstimatedReadDaysLeft(),
                expectedReadingSessionProgressTemplate.getEstimatedDaysLeft(),
                LocalDate.now().plusDays(expectedReadingSessionProgressTemplate.getEstimatedDaysLeft().intValue()).toString(),
                expectedReadingSessionProgressTemplate.getDeadline());

        assertThat(actualReadingSessionProgress.getLastReadPage()).isEqualTo(expectedReadingSessionProgress.getLastReadPage());
        assertThat(actualReadingSessionProgress.getPagesTotal()).isEqualTo(expectedReadingSessionProgress.getPagesTotal());
        assertThat(actualReadingSessionProgress.getReadPercentage()).isEqualTo(expectedReadingSessionProgress.getReadPercentage());
        assertThat(actualReadingSessionProgress.getAveragePagesPerDay()).isEqualTo(expectedReadingSessionProgress.getAveragePagesPerDay());
        assertThat(actualReadingSessionProgress.getEstimatedReadDaysLeft()).isEqualTo(expectedReadingSessionProgress.getEstimatedReadDaysLeft());
        assertThat(actualReadingSessionProgress.getEstimatedDaysLeft()).isGreaterThanOrEqualTo(expectedReadingSessionProgress.getEstimatedDaysLeft());
        assertThat(actualReadingSessionProgress.getEstimatedFinishDate()).isGreaterThanOrEqualTo(expectedReadingSessionProgress.getEstimatedFinishDate());
    }

    @Test
    public void getUserReadingSessionProgressWithThreeGapMixedReadings() throws Exception {
        Book book = getTestBook(BOOK_UUID + ".json");
        when(booksDao.getUserBook(JOHN_DOE_USER, BOOK_UUID)).thenReturn(Optional.of(book));

        ReadingSession readingSession = getTestReadingSession(READING_SESSION_UUID + "-three-gap-mixed-readings.json");
        when(readingSessionsDao.getUserReadingSession(JOHN_DOE_USER, BOOK_UUID, READING_SESSION_UUID)).thenReturn(Optional.of(readingSession));

        ReadingSessionProgress actualReadingSessionProgress = readingSessionsService.getUserReadingSessionProgress(JOHN_DOE_USER, BOOK_UUID, READING_SESSION_UUID);
        ReadingSessionProgress expectedReadingSessionProgressTemplate = getTestReadingSessionProgress(READING_SESSION_UUID + "-three-gap-readings-progress.json");
        ReadingSessionProgress expectedReadingSessionProgress = expectedReadingSessionProgressTemplate.copy(expectedReadingSessionProgressTemplate.getBookUuid(),
                expectedReadingSessionProgressTemplate.getLastReadPage(),
                expectedReadingSessionProgressTemplate.getPagesTotal(),
                expectedReadingSessionProgressTemplate.getReadPercentage(),
                expectedReadingSessionProgressTemplate.getAveragePagesPerDay(),
                expectedReadingSessionProgressTemplate.getEstimatedReadDaysLeft(),
                expectedReadingSessionProgressTemplate.getEstimatedDaysLeft(),
                LocalDate.now().plusDays(expectedReadingSessionProgressTemplate.getEstimatedDaysLeft().intValue()).toString(),
                expectedReadingSessionProgressTemplate.getDeadline());

        assertThat(actualReadingSessionProgress.getLastReadPage()).isEqualTo(expectedReadingSessionProgress.getLastReadPage());
        assertThat(actualReadingSessionProgress.getPagesTotal()).isEqualTo(expectedReadingSessionProgress.getPagesTotal());
        assertThat(actualReadingSessionProgress.getReadPercentage()).isEqualTo(expectedReadingSessionProgress.getReadPercentage());
        assertThat(actualReadingSessionProgress.getAveragePagesPerDay()).isEqualTo(expectedReadingSessionProgress.getAveragePagesPerDay());
        assertThat(actualReadingSessionProgress.getEstimatedReadDaysLeft()).isEqualTo(expectedReadingSessionProgress.getEstimatedReadDaysLeft());
        assertThat(actualReadingSessionProgress.getEstimatedDaysLeft()).isGreaterThanOrEqualTo(expectedReadingSessionProgress.getEstimatedDaysLeft());
        assertThat(actualReadingSessionProgress.getEstimatedFinishDate()).isGreaterThanOrEqualTo(expectedReadingSessionProgress.getEstimatedFinishDate());
    }

    @Test
    public void getUserReadingSessionProgressWithMissingBook() throws Exception {
        when(booksDao.getUserBook(JOHN_DOE_USER, BOOK_UUID)).thenReturn(Optional.empty());

        try {
            readingSessionsService.getUserReadingSessionProgress(JOHN_DOE_USER, BOOK_UUID, READING_SESSION_UUID);
            fail("It should fail with " + BooksException.Reason.BOOK_NOT_FOUND);
        } catch(BooksException ex) {
            assertThat(ex.getReason()).isEqualTo(BooksException.Reason.BOOK_NOT_FOUND);
        } catch (Exception ex) {
            fail("It should fail with " + BooksException.Reason.BOOK_NOT_FOUND);
        }
    }

    @Test
    public void getUserReadingSessionProgressWithMissingReadingSession() throws Exception {
        Book book = getTestBook(BOOK_UUID + ".json");
        when(booksDao.getUserBook(JOHN_DOE_USER, BOOK_UUID)).thenReturn(Optional.of(book));

        when(readingSessionsDao.getUserReadingSession(JOHN_DOE_USER, BOOK_UUID, READING_SESSION_UUID)).thenReturn(Optional.empty());

        try {
            readingSessionsService.getUserReadingSessionProgress(JOHN_DOE_USER, BOOK_UUID, READING_SESSION_UUID);
            fail("It should fail with " + ReadingSessionsException.Reason.READING_SESSION_NOT_FOUND);
        } catch(ReadingSessionsException ex) {
            assertThat(ex.getReason()).isEqualTo(ReadingSessionsException.Reason.READING_SESSION_NOT_FOUND);
        } catch (Exception ex) {
            fail("It should fail with " + ReadingSessionsException.Reason.READING_SESSION_NOT_FOUND);
        }
    }

    @Test
    public void getUserReadingSessionProgressWithEmptyDateReadingSessions() throws Exception {
        Book book = getTestBook(BOOK_UUID + ".json");
        when(booksDao.getUserBook(JOHN_DOE_USER, BOOK_UUID)).thenReturn(Optional.of(book));

        ReadingSession readingSession = getTestReadingSession(READING_SESSION_UUID + ".json");
        ReadingSession emptyReadingSession = readingSession.copy(readingSession.getUuid(),
                readingSession.getBookUuid(),
                readingSession.getDeadline(),
                Collections.emptyList());
        when(readingSessionsDao.getUserReadingSession(JOHN_DOE_USER, BOOK_UUID, READING_SESSION_UUID)).thenReturn(Optional.of(emptyReadingSession));

        try {
            readingSessionsService.getUserReadingSessionProgress(JOHN_DOE_USER, BOOK_UUID, READING_SESSION_UUID);
            fail("It should fail with " + ReadingSessionsException.Reason.DATE_READING_SESSION_NOT_FOUND);
        } catch(ReadingSessionsException ex) {
            assertThat(ex.getReason()).isEqualTo(ReadingSessionsException.Reason.DATE_READING_SESSION_NOT_FOUND);
        } catch (Exception ex) {
            fail("It should fail with " + ReadingSessionsException.Reason.DATE_READING_SESSION_NOT_FOUND);
        }
    }
}
