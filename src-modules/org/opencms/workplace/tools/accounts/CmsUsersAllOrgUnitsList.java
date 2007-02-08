/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/CmsUsersAllOrgUnitsList.java,v $
 * Date   : $Date: 2007/02/08 11:21:44 $
 * Version: $Revision: 1.1.2.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.tools.accounts;

import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsPrincipal;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * User account view over all manageable organizational units.<p>
 * 
 * @author Raphael Schnuck  
 * 
 * @version $Revision: 1.1.2.3 $ 
 * 
 * @since 6.5.6 
 */
public class CmsUsersAllOrgUnitsList extends A_CmsUsersList {

    /** list action id constant. */
    public static final String LIST_ACTION_OVERVIEW = "ao";

    /** list column id constant. */
    public static final String LIST_COLUMN_ORGUNIT = "co";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_ORGUNIT_DESC = "dd";

    /** list id constant. */
    public static final String LIST_ID = "lsuaou";

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsUsersAllOrgUnitsList(CmsJspActionElement jsp) {

        super(jsp, LIST_ID, Messages.get().container(Messages.GUI_USERS_LIST_NAME_0));
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsUsersAllOrgUnitsList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#executeListSingleActions()
     */
    public void executeListSingleActions() throws IOException, ServletException {

        String userId = getSelectedItem().getId();

        Map params = new HashMap();
        params.put(A_CmsEditUserDialog.PARAM_USERID, userId);
        params.put(A_CmsOrgUnitDialog.PARAM_OUFQN, getSelectedItem().get(LIST_COLUMN_ORGUNIT).toString().substring(1));
        // set action parameter to initial dialog call
        params.put(CmsDialog.PARAM_ACTION, CmsDialog.DIALOG_INITIAL);

        if (getParamListAction().equals(LIST_ACTION_OVERVIEW)) {
            // forward
            getToolManager().jspForwardTool(this, "/accounts/orgunit/users/edit", params);
        } else if (getParamListAction().equals(LIST_DEFACTION_EDIT)) {
            // forward to the edit user screen
            getToolManager().jspForwardTool(this, "/accounts/orgunit/users/edit", params);
        } else {
            super.executeListSingleActions();
        }
        listSave();
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#fillDetails(java.lang.String)
     */
    protected void fillDetails(String detailId) {

        super.fillDetails(detailId);

        List users = getList().getAllContent();
        Iterator itUsers = users.iterator();
        while (itUsers.hasNext()) {
            CmsListItem item = (CmsListItem)itUsers.next();
            String userName = item.get(LIST_COLUMN_LOGIN).toString();
            StringBuffer html = new StringBuffer(512);
            try {
                if (detailId.equals(LIST_DETAIL_ORGUNIT_DESC)) {
                    CmsUser user = readUser(userName);
                    html.append(OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), user.getOuFqn()).getDescription());
                } else {
                    continue;
                }
            } catch (Exception e) {
                // noop
            }
            item.set(detailId, html.toString());
        }
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#getGroupIcon()
     */
    protected String getGroupIcon() {

        return null;
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#getListItems()
     */
    protected List getListItems() throws CmsException {

        List listItems = super.getListItems();
        Iterator itListItems = listItems.iterator();
        while (itListItems.hasNext()) {
            CmsListItem item = (CmsListItem)itListItems.next();
            CmsUser user = getCms().readUser(new CmsUUID(item.getId()));
            item.set(LIST_COLUMN_ORGUNIT, CmsOrganizationalUnit.SEPARATOR + user.getOuFqn());
        }

        return listItems;
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#getUsers()
     */
    protected List getUsers() throws CmsException {

        return CmsPrincipal.filterCore(OpenCms.getRoleManager().getManageableUsers(getCms(), "", true));
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#readUser(java.lang.String)
     */
    protected CmsUser readUser(String name) throws CmsException {

        return getCms().readUser(name);
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setColumns(CmsListMetadata metadata) {

        super.setColumns(metadata);

        metadata.getColumnDefinition(LIST_COLUMN_GROUPS).setVisible(false);
        metadata.getColumnDefinition(LIST_COLUMN_SWITCH).setVisible(false);
        metadata.getColumnDefinition(LIST_COLUMN_ROLE).setVisible(false);
        metadata.getColumnDefinition(LIST_COLUMN_ACTIVATE).setVisible(false);
        metadata.getColumnDefinition(LIST_COLUMN_DELETE).setVisible(false);
        metadata.getColumnDefinition(LIST_COLUMN_LASTLOGIN).setVisible(false);

        // add column for orgunit
        CmsListColumnDefinition orgUnitCol = new CmsListColumnDefinition(LIST_COLUMN_ORGUNIT);
        orgUnitCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_ORGUNIT_0));
        orgUnitCol.setWidth("30%");
        metadata.addColumn(orgUnitCol, metadata.getColumnDefinitions().indexOf(
            metadata.getColumnDefinition(LIST_COLUMN_NAME)));
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#setDeleteAction(org.opencms.workplace.list.CmsListColumnDefinition)
     */
    protected void setDeleteAction(CmsListColumnDefinition deleteCol) {

        // noop
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#setEditAction(org.opencms.workplace.list.CmsListColumnDefinition)
     */
    protected void setEditAction(CmsListColumnDefinition editCol) {

        CmsListDirectAction editAction = new CmsListDirectAction(LIST_ACTION_OVERVIEW);
        editAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_DEFACTION_EDIT_NAME_0));
        editAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_DEFACTION_EDIT_HELP_0));
        editAction.setIconPath(PATH_BUTTONS + "user.png");
        editCol.addDirectAction(editAction);
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setIndependentActions(CmsListMetadata metadata) {

        super.setIndependentActions(metadata);

        // add orgunit description details
        CmsListItemDetails orgUnitDescDetails = new CmsListItemDetails(LIST_DETAIL_ORGUNIT_DESC);
        orgUnitDescDetails.setAtColumn(LIST_COLUMN_DISPLAY);
        orgUnitDescDetails.setVisible(false);
        orgUnitDescDetails.setShowActionName(Messages.get().container(
            Messages.GUI_USERS_DETAIL_SHOW_ORGUNIT_DESC_NAME_0));
        orgUnitDescDetails.setShowActionHelpText(Messages.get().container(
            Messages.GUI_USERS_DETAIL_SHOW_ORGUNIT_DESC_HELP_0));
        orgUnitDescDetails.setHideActionName(Messages.get().container(
            Messages.GUI_USERS_DETAIL_HIDE_ORGUNIT_DESC_NAME_0));
        orgUnitDescDetails.setHideActionHelpText(Messages.get().container(
            Messages.GUI_USERS_DETAIL_HIDE_ORGUNIT_DESC_HELP_0));
        orgUnitDescDetails.setName(Messages.get().container(Messages.GUI_USERS_DETAIL_ORGUNIT_DESC_NAME_0));
        orgUnitDescDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(
            Messages.GUI_USERS_DETAIL_ORGUNIT_DESC_NAME_0)));
        metadata.addItemDetails(orgUnitDescDetails);

        metadata.getSearchAction().addColumn(metadata.getColumnDefinition(LIST_COLUMN_EMAIL));
        metadata.getSearchAction().addColumn(metadata.getColumnDefinition(LIST_COLUMN_ORGUNIT));
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setMultiActions(CmsListMetadata metadata) {

        // noop
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#validateParamaters()
     */
    protected void validateParamaters() throws Exception {

        // no param check needed
    }
}
