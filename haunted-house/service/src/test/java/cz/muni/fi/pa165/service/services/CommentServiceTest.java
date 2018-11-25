package cz.muni.fi.pa165.service.services;

import cz.muni.fi.pa165.dao.CommentDao;
import cz.muni.fi.pa165.entity.Bogeyman;
import cz.muni.fi.pa165.entity.Comment;
import cz.muni.fi.pa165.entity.House;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.dao.RecoverableDataAccessException;
import org.testng.Assert;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * @author Lukas Sadlek
 */
public class CommentServiceTest {
    private CommentService commentService;

    @Mock
    private CommentDao commentDao;

    private Comment comment1, comment2, comment3;

    @Mock
    private House house1;

    @Mock
    private Bogeyman bogeyman;

    @Mock
    private Comment comment4;

    private List<Comment> comments = new ArrayList<>();
    private List<Comment> houseComments = new ArrayList<>();
    private LocalDate firstDay;
    private LocalDate theDayBefore;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        comment1 = new Comment();
        comment2 = new Comment();
        comment3 = new Comment();

        firstDay = LocalDate.of(2018, 10, 28);
        theDayBefore = LocalDate.of(2018, 10, 27);

        comment1.setAuthor("Jirina");
        comment1.setDate(firstDay);
        comment1.setText("Na pokoji cislo 11 se kazdy den o sedme rano zjevi duch stareho namornika. " +
                "Vstane z postele, oblekne se, vezme si svuj kufr a odejde pryc. " +
                "Na ubytovani na pokoji 11 nabizeji vyraznou slevu.");

        comment2.setAuthor("Anonym");
        comment2.setDate(firstDay);
        comment2.setText("Prilis daleko do centra");

        comment3.setAuthor("Anonym");
        comment3.setDate(theDayBefore);
        comment3.setText("Nemam slov");

        comment1.setHouse(house1);
        comment2.setHouse(house1);

        comments.add(comment1);
        comments.add(comment2);
        comments.add(comment3);

        houseComments.add(comment2);
        houseComments.add(comment1);

        commentService = new CommentServiceImpl(commentDao);
    }

    @Test
    public void testFindById() {
        when(commentDao.findById(1L)).thenReturn(comment1);
        Comment comment = commentService.findById(1L);
        Assert.assertEquals(comment, comment1);
        verify(commentDao).findById(1L);
    }

    @Test
    public void testFindAll() {
        when(commentDao.findAll()).thenReturn(comments);
        List<Comment> resultComments = commentService.findAll();
        Assert.assertEquals(resultComments.size(), 3);
        verify(commentDao).findAll();
    }

    @Test
    public void testFindByAuthor() {
        when(commentDao.findByAuthor("Jirina")).thenReturn(Collections.singletonList(comment1));
        List<Comment> resultComments = commentService.findByAuthor("Jirina");
        Assert.assertEquals(resultComments.size(), 1);
        Assert.assertEquals(resultComments.get(0), comment1);
        verify(commentDao).findByAuthor("Jirina");
    }

    @Test
    public void testFindByHouse() {
        when(commentDao.findByHouse(house1)).thenReturn(houseComments);
        List<Comment> resultComments = commentService.findByHouse(house1);
        Assert.assertEquals(resultComments.size(), 2);
        Assert.assertTrue(resultComments.contains(comment1));
        Assert.assertTrue(resultComments.contains(comment2));
        verify(commentDao).findByHouse(house1);
    }

    @Test
    public void testGetHouse() {
        when(comment4.getHouse()).thenReturn(house1);
        House house = commentService.getHouse(comment4);
        Assert.assertEquals(house, house1);
        verify(comment4).getHouse();
    }

    @Test
    public void testIsCommentedHouseHaunted() {
        when(comment4.getHouse()).thenReturn(house1);
        when(house1.getBogeymen()).thenReturn(Collections.singletonList(bogeyman));
        Assert.assertTrue(commentService.isCommentedHouseHaunted(comment4));
        verify(comment4).getHouse();
        verify(house1).getBogeymen();
    }

    @Test
    public void testCreate() {
        commentService.create(comment1);
        verify(commentDao).create(comment1);
    }

    @Test
    public void testDelete() {
        commentService.delete(comment1);
        verify(commentDao).delete(comment1);
    }

    @Test
    public void testUpdate() {
        commentService.update(comment2);
        verify(commentDao).update(comment2);
    }

    @Test
    public void testFindAllSortedByAuthor() {
        when(commentDao.findAll()).thenReturn(comments);
        List<Comment> resultComments = commentService.findAllSortedByAuthor();
        Assert.assertEquals(resultComments.size(), 3);;
        Assert.assertEquals(resultComments.get(0).getAuthor(), "Anonym");
        Assert.assertEquals(resultComments.get(1).getAuthor(), "Anonym");
        Assert.assertEquals(resultComments.get(2), comment1);
        verify(commentDao).findAll();
    }

    @Test
    public void findAllSortedByDate() {
        when(commentDao.findAll()).thenReturn(comments);
        List<Comment> resultComments = commentService.findAllSortedByAuthor();
        Assert.assertEquals(resultComments.size(), 3);
        Assert.assertEquals(resultComments.get(0).getDate(), firstDay);
        Assert.assertEquals(resultComments.get(1).getDate(), firstDay);
        Assert.assertEquals(resultComments.get(2), comment3);
    }

    @Test
    public void testExceptionsOnPersistenceLayer() {
        when(commentDao.findAll()).thenThrow(new RecoverableDataAccessException(""));
        when(commentDao.findByHouse(house1)).thenThrow(new QueryTimeoutException(""));
        when(commentDao.findByAuthor("Anonym")).thenThrow(new IllegalArgumentException(""));
        when(commentDao.findById(12L)).thenThrow(new RecoverableDataAccessException(""));
        Assert.assertThrows(DataAccessException.class, () -> commentService.findAll());
        Assert.assertThrows(DataAccessException.class, () -> commentService.findByHouse(house1));
        Assert.assertThrows(DataAccessException.class, () -> commentService.findByAuthor("Anonym"));
        Assert.assertThrows(DataAccessException.class, () -> commentService.findById(12L));
        Assert.assertThrows(DataAccessException.class, () -> commentService.findAllSortedByAuthor());
        Assert.assertThrows(DataAccessException.class, () -> commentService.findAllSortedByDate());
    }

}
