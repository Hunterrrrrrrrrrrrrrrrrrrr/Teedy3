package com.sismics.docs.core.dao.jpa;

import com.sismics.docs.BaseTransactionalTest;
import com.sismics.docs.core.dao.GroupDao;
import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.dao.criteria.GroupCriteria;
import com.sismics.docs.core.dao.dto.GroupDto;
import com.sismics.docs.core.model.jpa.Group;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.model.jpa.UserGroup;
import com.sismics.docs.core.util.TransactionUtil;
import com.sismics.docs.core.util.authentication.InternalAuthenticationHandler;
import com.sismics.docs.core.util.jpa.SortCriteria;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.List;

/**
 * Tests the persistance layer.
 * 
 * @author jtremeaux
 */
public class TestJpa extends BaseTransactionalTest {
    @Test
    public void testJpa() throws Exception {
        // Create a user
        UserDao userDao = new UserDao();
        User user = createUser("testJpa");

        TransactionUtil.commit();

        // Search a user by his ID
        user = userDao.getById(user.getId());
        Assert.assertNotNull(user);
        Assert.assertEquals("toto@docs.com", user.getEmail());

        // Authenticate using the database
        Assert.assertNotNull(new InternalAuthenticationHandler().authenticate("testJpa", "12345678"));

        // Delete the created user
        userDao.delete("testJpa", user.getId());
        TransactionUtil.commit();
    }

    @Test
    public void testJpa2() throws Exception {
        UserDao userDao = new UserDao();
        User testUser = createUser("groupTest");
        TransactionUtil.commit();

        GroupDao groupDao = new GroupDao();
        Group rootGroup = new Group();
        rootGroup.setName("testGroup");
        String rootGroupId = groupDao.create(rootGroup, testUser.getId());
        TransactionUtil.commit();

        rootGroup.setName("updatedGroup");
        groupDao.update(rootGroup, testUser.getId());
        TransactionUtil.commit();

        Group refreshedGroup = groupDao.getActiveById(rootGroupId);
        Assert.assertNotNull("Group should not be null", refreshedGroup);
        Assert.assertEquals("updatedGroup", refreshedGroup.getName());

        Group byName = groupDao.getActiveByName("updatedGroup");
        Assert.assertNotNull("Group should be retrievable by name", byName);
        Assert.assertEquals(rootGroupId, byName.getId());

        UserGroup membership = new UserGroup();
        membership.setUserId(testUser.getId());
        membership.setGroupId(rootGroupId);
        String membershipId = groupDao.addMember(membership);
        TransactionUtil.commit();

        Group subGroup = new Group();
        subGroup.setName("childGroup");
        subGroup.setParentId(rootGroupId);
        String subGroupId = groupDao.create(subGroup, testUser.getId());
        TransactionUtil.commit();

        GroupCriteria searchCriteria = new GroupCriteria();
        searchCriteria.setSearch("updated");
        List<GroupDto> matchedGroups = groupDao.findByCriteria(searchCriteria, null);
        Assert.assertFalse("Should find at least one group", matchedGroups.isEmpty());
        Assert.assertEquals("Group ID should match", rootGroupId, matchedGroups.get(0).getId());

        searchCriteria = new GroupCriteria();
        searchCriteria.setUserId(testUser.getId());
        searchCriteria.setRecursive(true);
        matchedGroups = groupDao.findByCriteria(searchCriteria, null);
        Assert.assertTrue("Recursive group search should return results", matchedGroups.size() >= 1);

        groupDao.removeMember(rootGroupId, testUser.getId());
        TransactionUtil.commit();

        groupDao.delete(subGroupId, testUser.getId());
        groupDao.delete(rootGroupId, testUser.getId());
        TransactionUtil.commit();

        Assert.assertNull("Group should be deleted", groupDao.getActiveById(rootGroupId));
        Assert.assertNull("Child group should be deleted", groupDao.getActiveById(subGroupId));

        userDao.delete("groupTest", testUser.getId());
        TransactionUtil.commit();
    }
}
