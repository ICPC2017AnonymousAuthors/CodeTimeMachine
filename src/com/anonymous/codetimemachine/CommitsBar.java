package com.anonymous.codetimemachine;

import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class CommitsBar extends CommitsBarBase
{

    protected JBScrollPane commitsBarView = null;
    protected JPanel thisComponentWithoutScroll = null;

    int currentHovredItem_localUIIndex = INVALID;

    public enum CommitItemDirection {NONE, LTR, RTL};


    private CommitUIItem[] commitUIItems /* Most recent commit at 0*/;
    CommitItemDirection direction = CommitItemDirection.NONE;
    int contentHeight =0;

    int myLastActive_cIndex = -1;


    public CommitsBar(CommitItemDirection direction, ClassType s, TTMSingleFileView TTMWindow)
    {
        this.TTMWindow = TTMWindow;
        this.classType= s;
        this.direction = direction;

        thisComponentWithoutScroll = createEmptyJComponent();
        addScrollToThisComponent(thisComponentWithoutScroll);

        //setupToolTipSetting();
    }


    private int findCommitUIItemIndexFromcIndex(int cIndex)
    {
        int commitUIItemIndexIfExist = -1;
        for(int i=0; i<commitList.size(); i++)
            if(commitList.get(i).cIndex == cIndex)
            {
                commitUIItemIndexIfExist = i;
                break;
            }

        return commitUIItemIndexIfExist;
    }

    public void activateCommitUIItemIfExists(int cIndex)
    {
        TTMWindow.activeCommit_cIndex = cIndex;
        int commitUIItemIndexIfExist = findCommitUIItemIndexFromcIndex(cIndex);

        if(commitUIItemIndexIfExist != -1)
            commitUIItems[commitUIItemIndexIfExist].setActivated(true);
    }

    @Override
    public void updateCommitsList(ArrayList<CommitWrapper> newCommitList)
    {
        this.commitList = newCommitList;

        thisComponentWithoutScroll.removeAll();

        creatingCommitsUIItem(newCommitList);

        activateCommitUIItemIfExists(TTMWindow.activeCommit_cIndex);

        // The dimension of Scroll::innerPanel is as below
        //
        Dimension newDimension  = new Dimension(COMMITS_BAR_VIEW_DIMENSION.width-30/*because of the scroll-handle of parent scrollContainer*/, contentHeight);
        thisComponentWithoutScroll.setPreferredSize(newDimension);
        thisComponentWithoutScroll.setSize(newDimension);
        thisComponentWithoutScroll.setMaximumSize(newDimension);
        thisComponentWithoutScroll.setMinimumSize(newDimension);


        commitsBarView.repaint();
        scrollToBottom(commitsBarView);
    }

    private void scrollToBottom(JScrollPane scrollPane) {
        JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
        AdjustmentListener downScroller = new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                Adjustable adjustable = e.getAdjustable();
                adjustable.setValue(adjustable.getMaximum());
                verticalBar.removeAdjustmentListener(this);
            }
        };
        verticalBar.addAdjustmentListener(downScroller);
    }

    private void setupToolTipSetting()
    {
        ToolTipManager.sharedInstance().setEnabled(true);
        ToolTipManager.sharedInstance().setInitialDelay(0); // it needs ToolTipManager.sharedInstance().setEnabled(true); before
    }

    private void addScrollToThisComponent(JPanel internalComponent)
    {
        commitsBarView = new JBScrollPane();
        commitsBarView.setViewportView(internalComponent);
        commitsBarView.setBorder(null);
        commitsBarView.setPreferredSize(COMMITS_BAR_VIEW_DIMENSION); //The Parent GroupLayout respects the .setPreferredSize()
        commitsBarView.getHorizontalScrollBar().setForeground(Color.BLACK); //default WHITE

        // BoxLayout cannot handle different alignments: see http://download.oracle.com/javase/tutorial/uiswing/layout/box.html
        //commitsBarView.setMaximumSize(new Dimension(commitItems[0].getComponent().getSize().width+10, contentHeight+10));
        commitsBarView.setMaximumSize(COMMITS_BAR_VIEW_DIMENSION);

        commitsBarView.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    }

    private void creatingCommitsUIItem(ArrayList<CommitWrapper> commitList)
    {
        if(commitList.size()==0) return;
        // commitList: Most recent commit at 0

        Calendar currentCommitCal = Calendar.getInstance();
        Calendar lastCommitCal = Calendar.getInstance();
        lastCommitCal.setTime(new Date(Long.MIN_VALUE));

        // UI_items includes CommitItem and other fake UI elements
        ArrayList<JComponent> UI_items = new ArrayList<>(commitList.size()/*reserve at least this much memory*/);

        commitUIItems = new CommitUIItem[commitList.size()];

        for(int i=0; i<commitList.size() ; i++)
        {
            currentCommitCal.setTime(commitList.get(i).getDate());
            boolean sameDay = lastCommitCal.get(Calendar.YEAR) == currentCommitCal.get(Calendar.YEAR) &&
                    lastCommitCal.get(Calendar.DAY_OF_YEAR) == currentCommitCal.get(Calendar.DAY_OF_YEAR);
            /////

            if(!sameDay)
            {
                if(i!=0)
                {
                    NewDayItem newDayMarker = new NewDayItem(direction, commitList.get(i-1).getDate());
                    UI_items.add(newDayMarker.getComponent());
                }

            }

            commitUIItems[i]= new CommitUIItem(direction, i, commitList.get(i), this);

            UI_items.add(commitUIItems[i].getComponent());
            ///
            lastCommitCal.setTime(commitList.get(i).getDate());
        }

        NewDayItem newDayMarker = new NewDayItem(direction, commitList.get(commitList.size()-1).getDate());
        UI_items.add(newDayMarker.getComponent());


        contentHeight = 0;
        for(int i=UI_items.size()-1; i>=0 ; i--)
        {
            thisComponentWithoutScroll.add(UI_items.get(i));

            //final int GAP_H = 10;
            //thisComponentWithoutScroll.add(Box.createRigidArea(new Dimension(1, GAP_H)));
            contentHeight += UI_items.get(i).getSize().height ;//+ GAP_H;

        }
    }

    private JPanel createEmptyJComponent()
    {
        // Size of this component according to children's components => CommitItem
        JPanel c = new JPanel();
        BoxLayout boxLayout = new BoxLayout(c, BoxLayout.PAGE_AXIS);
        c.setLayout(boxLayout);

        c.setBackground(BG_COLOR);
        if(CommonValues.IS_UI_IN_DEBUGGING_MODE)
            c.setBackground(Color.RED);

        return c;
    }

    @Override
    /*Get Active_cIndex from TTMWindow*/
    public void setActiveCommit_cIndex()
    {
        if( myLastActive_cIndex!=-1)
        {
            int x = findCommitUIItemIndexFromcIndex(myLastActive_cIndex);
            if(x!= -1)
            {
                /*If we are here, it means last active commit was also in the current CommitsBar list*/
                commitUIItems[x].setActivated(false);
            }
        }

        // we keep it here, because anytime from anywhere somebody may change TTMWindow.activeCommit_cIndex and call this function
        myLastActive_cIndex = TTMWindow.activeCommit_cIndex;

        int x = findCommitUIItemIndexFromcIndex(TTMWindow.activeCommit_cIndex);
        if(x!= -1)
        {
                /*If we are here, it means new active commit was also in the current CommitsBar list*/
            commitUIItems[x].setActivated(true);
        }
    }


    private void updateHovredItem(int hoveredCommitUIItemIndex, boolean newStatus)
    {

        if(newStatus == true)
        {
            if(currentHovredItem_localUIIndex==hoveredCommitUIItemIndex) return;
            currentHovredItem_localUIIndex = hoveredCommitUIItemIndex;
            int newHoveredItem_cIndex = commitList.get(hoveredCommitUIItemIndex).cIndex;
            TTMWindow.updateTopLayerCommitsInfoData(newHoveredItem_cIndex);
        }
        else
        {
            if(currentHovredItem_localUIIndex!=hoveredCommitUIItemIndex)
            {
                // It means, probably, we moved from A to B. B has already call this with "true" status,
                // and the A is calling this function with "false" status. So, we shouldn't invisible top bar. because
                // it's showing B's info.
                return;
            }
            currentHovredItem_localUIIndex = INVALID;
            TTMWindow.updateTopLayerCommitsInfoData(INVALID);
        }


    }

    private void activateCommit(int clickedCommitUIItemIndex)
    {
        int newActivecommit_cIndex = commitList.get(clickedCommitUIItemIndex).cIndex;
        TTMWindow.activeCommit_cIndex = newActivecommit_cIndex;
        TTMWindow.navigateToCommit(classType, newActivecommit_cIndex);
        setActiveCommit_cIndex();
    }

    @Override
    public JComponent getComponent()
    {
        return commitsBarView;
    }

    private class NewDayItem
    {
        ///////// ++ Constant ++ /////////
        private final Dimension COMPONENT_SIZE = new Dimension( COMMITS_BAR_VIEW_DIMENSION.width,30 );
        private final Dimension MARKERT_NORMAL_SIZE = new Dimension( 10,5 );//
        private final Color NORMAL_COLOR = Color.WHITE;
        ///////// ++ Constant -- /////////

        ///////// ++ UI ++ /////////
        private JPanel myComponent;
        private JLabel marker, commitInfo;
        ///////// ++ UI -- /////////

        private CommitItemDirection direction;
        private Date date;

        public NewDayItem(CommitItemDirection direction, Date date)
        {
            this.direction = direction;
            this.date = date;

            createEmptyJComponent();

            if(direction == CommitItemDirection.LTR)
                myComponent.setAlignmentX(Component.LEFT_ALIGNMENT); // make it left_align within parent layout (Hbox)
            else
                myComponent.setAlignmentX(Component.RIGHT_ALIGNMENT);


            myComponent.setBackground(CommonValues.APP_COLOR_THEME);
            if(CommonValues.IS_UI_IN_DEBUGGING_MODE)
                myComponent.setBackground(Color.YELLOW);


            setupUI_marker();
            setupUI_commitInfo();

            updateUIToNewSize();
            setupComponentResizingBehaviour();
        }

        private void setupUI_marker()
        {
            marker = new JLabel("");
            marker.setOpaque(true);
            marker.setBackground(NORMAL_COLOR);
            myComponent.add(marker);
        }

        private void setupUI_commitInfo()
        {
            String commitInfoStr = "";


            Calendar cal = Calendar.getInstance(); //today by default
            cal.add(Calendar.DATE, -1);
            Date yesterday = cal.getTime();

            if(CalendarHelper.isSameDay(new Date(), date))
            {
                commitInfoStr = "Today";
            }
            else if(CalendarHelper.isSameDay(yesterday, date))
            {
                commitInfoStr = "Yesterday";
            }
            else
            {
                commitInfoStr = CalendarHelper.convertDateToStringYMD(date);
            }

            commitInfo = new JLabel(commitInfoStr);

            if(CommonValues.IS_UI_IN_DEBUGGING_MODE)
            {
                commitInfo.setOpaque(true);
                commitInfo.setBackground(Color.CYAN);
            }
            Font font = commitInfo.getFont();
            Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
            commitInfo.setFont(boldFont);
            commitInfo.setForeground(NORMAL_COLOR);
            if(direction == CommitItemDirection.LTR)
                commitInfo.setHorizontalAlignment(SwingConstants.LEFT);
            else
                commitInfo.setHorizontalAlignment(SwingConstants.RIGHT);
            commitInfo.setSize(myComponent.getSize().width-30,20);
            myComponent.add(commitInfo);
        }

        private void createEmptyJComponent()
        {
            myComponent = new JPanel(null);
            myComponent.setSize(COMPONENT_SIZE);
            myComponent.setPreferredSize(COMPONENT_SIZE);
            myComponent.setMinimumSize(COMPONENT_SIZE);
            myComponent.setMaximumSize(COMPONENT_SIZE);
        }

        private String getMonthName(int month){
            //String[] monthNames = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
            String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            return monthNames[month];
        }

        private void updateMarkerLocation()
        {
            if(direction== CommitItemDirection.LTR)
                marker.setLocation( 0/*Align Left*/,
                        myComponent.getSize().height/2 - marker.getSize().height/2);
            else
            {
                marker.setLocation( myComponent.getSize().width - marker.getSize().width/*Align Right*/,
                        myComponent.getSize().height / 2 - marker.getSize().height / 2);
            }
        }

        private void updateCommitInfoLocation()
        {
            final int DELTA_DIS_FROM_MARKER = 3;
            if(direction== CommitItemDirection.LTR)
                commitInfo.setLocation( marker.getLocation().x+marker.getSize().width+DELTA_DIS_FROM_MARKER,
                        marker.getLocation().y+marker.getSize().height/2-commitInfo.getSize().height/2);
            else
            {
                commitInfo.setLocation( marker.getLocation().x - DELTA_DIS_FROM_MARKER - commitInfo.getSize().width,
                        marker.getLocation().y + marker.getSize().height / 2 - commitInfo.getSize().height / 2);
            }
        }

        private void updateUIToNewSize()
        {
            marker.setSize(MARKERT_NORMAL_SIZE);

            updateMarkerLocation();

            updateCommitInfoLocation();
        }

        private void setupComponentResizingBehaviour()
        {
            myComponent.addComponentListener(new ComponentListener()
            {
                @Override
                public void componentResized(ComponentEvent e)
                {
                    /*
                    Since thisComponentWithoutScroll may change (since it belongs to a layout AND we didn't limit the maximum size),
                    we need to reaarange objects when size chnages.
                    if thisComponentWithoutScroll had layout (for its children) we wouldn't manage its children after each size change.
                     */
                    updateUIToNewSize();
                }

                @Override
                public void componentMoved(ComponentEvent e)
                {
                }

                @Override
                public void componentShown(ComponentEvent e)
                {
                }

                @Override
                public void componentHidden(ComponentEvent e)
                {
                }
            });
        }

        public JPanel getComponent()
        {
            return myComponent;
        }
    }

    static private class CommitUIItem
    {
        ///////// ++ Constant ++ /////////
        private final Dimension COMPONENT_SIZE = new Dimension( 80,20 );
        private final int LONG_FACTOR = 5;
        private final Dimension MARKERT_NORMAL_SIZE = new Dimension( 17,5 );
        private final Dimension MARKER_HOVERED_SIZE = new Dimension( 20,8 );
        //
        private final Color NORMAL_COLOR = Color.LIGHT_GRAY;
        private final Color HOVERED_COLOR = new Color(255,0,0,200);
        ///////// ++ Constant -- /////////

        ///////// ++ UI ++ /////////
        private JPanel myComponent;
        private JLabel marker, commitInfo;
        ///////// ++ UI -- /////////


        private int commitUIItemIndex=-1;
        private CommitItemDirection direction;

        private boolean isActive=false;
        private CommitsBar commitsBar=null;



        public CommitUIItem(CommitItemDirection direction, int commitUIItemIndex, CommitWrapper commitWrapper, CommitsBar commitBar)
        {
            this.commitsBar = commitBar;
            this.direction = direction;
            this.commitUIItemIndex = commitUIItemIndex;

            setupUI(commitWrapper);

            setupMouseBeahaviour();
            setupComponentResizingBehaviour();
        }

        private void setupUI(CommitWrapper commitWrapper)
        {
            createEmptyJComponent();
            if(direction == CommitItemDirection.LTR)
                myComponent.setAlignmentX(Component.LEFT_ALIGNMENT); // make it left_align within parent layout (Hbox)
            else
                myComponent.setAlignmentX(Component.RIGHT_ALIGNMENT);

            String tooltipText = "<html>" + "<body bgcolor=\"#C0C0C0\">"
                                    + "<h4 style=\"color:#3C3C3C;\">"
                                        + "&nbsp Commit-Date: " + CalendarHelper.convertDateToStringYMDHM(commitWrapper.getDate()) + "<br>"
                                        + "&nbsp Commit-Id: " + commitWrapper.getCommitID() + "<br>"
                                        + "&nbsp Author: " + commitWrapper.getAuthor() + "<br>"
                                        + "&nbsp Commit-message: " + commitWrapper.getCommitMessage()
                                    + "</h4>"
                                + "</body>" + "</html>";
            //myComponent.setToolTipText(tooltipText);

            myComponent.setBackground(CommonValues.APP_COLOR_THEME);
            if(CommonValues.IS_UI_IN_DEBUGGING_MODE)
                myComponent.setBackground(Color.GREEN);

            setupUI_marker();
            setupUI_commitInfo(commitWrapper);

            updateToNormalUI();
        }

        private void setupComponentResizingBehaviour()
        {
            myComponent.addComponentListener(new ComponentListener()
            {
                @Override
                public void componentResized(ComponentEvent e)
                {
                    /*
                    Since thisComponentWithoutScroll may change (since it belongs to a layout AND we didn't limit the maximum size),
                    we need to reaarange objects when size chnages.
                    if thisComponentWithoutScroll had layout (for its children) we wouldn't manage its children after each size change.
                     */
                    int sd=0;
                    sd++;
                    updateToNormalUI();
                }

                @Override
                public void componentMoved(ComponentEvent e)
                {
                    int sd=0;
                    sd++;
                }

                @Override
                public void componentShown(ComponentEvent e)
                {
                    int sd=0;
                    sd++;
                }

                @Override
                public void componentHidden(ComponentEvent e)
                {
                    int sd=0;
                    sd++;
                }
            });
        }

        private void setupUI_marker()
        {
            marker = new JLabel("");
            marker.setOpaque(true);
            myComponent.add(marker);
        }

        private void setupUI_commitInfo(CommitWrapper commitWrapper)
        {
            String commitInfoStr = "";

            if(commitWrapper.isFake())
                commitInfoStr = "*Now*";
            else
            {
                commitInfoStr = CalendarHelper.convertDateToTime(commitWrapper.getDate());
            }


            commitInfo = new JLabel(commitInfoStr);
            if(CommonValues.IS_UI_IN_DEBUGGING_MODE)
            {
                commitInfo.setOpaque(true);
                commitInfo.setBackground(Color.CYAN);
            }

            Font font = commitInfo.getFont();
            Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
            commitInfo.setFont(boldFont);
            if(direction == CommitItemDirection.LTR)
                commitInfo.setHorizontalAlignment(SwingConstants.LEFT);
            else
                commitInfo.setHorizontalAlignment(SwingConstants.RIGHT);
            commitInfo.setSize(myComponent.getSize().width-30,20);
            myComponent.add(commitInfo);
        }

        private void setupMouseBeahaviour()
        {
            myComponent.addMouseListener(new MouseListener()
            {
                @Override
                public void mouseClicked(MouseEvent e) {}

                @Override
                public void mousePressed(MouseEvent e) {}

                @Override
                public void mouseReleased(MouseEvent e)
                {
                    commitsBar.activateCommit(commitUIItemIndex);
                }

                @Override
                public void mouseEntered(MouseEvent e)
                {
                    commitsBar.updateHovredItem(commitUIItemIndex, true);

                    if(!isActive)
                    {
                        updateToActiveUI();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e)
                {
                    commitsBar.updateHovredItem(commitUIItemIndex, false);
                    if(!isActive)
                    {
                        updateToNormalUI();
                    }
                }
            });
        }

        private void createEmptyJComponent()
        {
            myComponent = new JPanel(null);
            myComponent.setSize(COMPONENT_SIZE);
            myComponent.setPreferredSize(COMPONENT_SIZE);
            myComponent.setMinimumSize(COMPONENT_SIZE);
            myComponent.setMaximumSize(COMPONENT_SIZE);
        }

        private void updateToNormalUI()
        {
            marker.setSize(MARKERT_NORMAL_SIZE);
            marker.setBackground(NORMAL_COLOR);

            updateMarkerLocation();
            commitInfo.setForeground(NORMAL_COLOR);
            updateCommitInfoLocation();
        }

        private void updateToActiveUI()
        {
            marker.setSize(MARKER_HOVERED_SIZE);
            marker.setBackground(HOVERED_COLOR);
            updateMarkerLocation();

            updateCommitInfoLocation();
            commitInfo.setForeground(HOVERED_COLOR);
        }

        private void updateMarkerLocation()
        {
            if(direction== CommitItemDirection.LTR)
                marker.setLocation( 0/*Align Left*/,
                        myComponent.getSize().height/2 - marker.getSize().height/2);
            else
            {
                marker.setLocation( myComponent.getSize().width - marker.getSize().width/*Align Right*/,
                        myComponent.getSize().height / 2 - marker.getSize().height / 2);
            }
        }

        private void updateCommitInfoLocation()
        {
            final int DELTA_DIS_FROM_MARKER = 3;
            if(direction== CommitItemDirection.LTR)
                commitInfo.setLocation( marker.getLocation().x+marker.getSize().width+DELTA_DIS_FROM_MARKER,
                        marker.getLocation().y+marker.getSize().height/2-commitInfo.getSize().height/2);
            else
            {
                commitInfo.setLocation( marker.getLocation().x - DELTA_DIS_FROM_MARKER - commitInfo.getSize().width,
                        marker.getLocation().y + marker.getSize().height / 2 - commitInfo.getSize().height / 2);
            }
        }

        private void setActivated(boolean newStatus)
        {
            isActive = newStatus;
            if(isActive)
                updateToActiveUI();
            else
                updateToNormalUI();
        }

        public JPanel getComponent()
        {
            return myComponent;
        }
    }
}

