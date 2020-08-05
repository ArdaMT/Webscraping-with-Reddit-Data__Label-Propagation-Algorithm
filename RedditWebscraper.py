

# coding: utf-8

# In[1]:

from bs4 import BeautifulSoup as bs
import praw
import prettytable as pt
import matplotlib.pyplot as plt
get_ipython().magic(u'matplotlib inline')
import numpy as ny
import urllib

modList=[]
subredditList=[]
subscriberDict=dict()
clearModList=[]

#read the first reddit page
redditPage1 = "http://redditlist.com/sfw"
r=urllib.urlopen(redditPage1).read()
soup1 =bs(r,'lxml')
subscriberIndex=0

#add  subreddits and the number of subscribers for each subreddit from the first reddit page to the lists
for container in soup1.select(".listing"):
    if container.select("h3")[0].text=="Subscribers":
        for subreddit in container.select(".listing-item"):
            subredditList.append(subreddit['data-target-subreddit'])
        for subreddit in container.select(".listing-stat"):
            subscriberDict [subscriberIndex]=subreddit.text
            subscriberIndex+=1
    
# loop through pages 2 to 8 of reddit,
# Add  subreddits and the number of subscribers for each subreddit from each page to the lists
for i in range(2,9):
    #here we update the reddit page number with each loop and declare a new soup using this info
    redditPage = 'http://redditlist.com/sfw?page={}'.format(i)
    r=urllib.urlopen(redditPage).read()
    soup =bs(r,'lxml')
    for container in soup.select(".listing"):
        if container.select("h3")[0].text=="Subscribers":
            for subreddit in container.select(".listing-item"):
                subredditList.append(subreddit['data-target-subreddit'])
            for subreddit in container.select(".listing-stat"):
                subscriberDict [subscriberIndex]=subreddit.text
                subscriberIndex+=1
                
# Integrate praw using the provided developer information on reddit's webpage
reddit = praw.Reddit(client_id='u33TJzGIvE_8aw',
                     client_secret='iTbbxYxg1UsVUvw0D2Lef3L9EBs',
                     user_agent='ar_mel_')

# Extract the moderators for each subreddit and save in a list
for i in subredditList:
    modList.append(reddit.subreddit(i).moderator())

# Make a new moderator list and leave out  AutoModerator while copying the elements of modlist.
for i in range (len(modList)):
    a=[]
    for j in range(len(modList[i])):
        if (modList[i][j]!='AutoModerator'):
            a.append(modList[i][j])
    clearModList.append(a)

# Create a list of sets from clearModList for an accelerated pairwise comparison
clearModSet = [set(i) for i in clearModList]
# Compare the elements of two lists given in the argument and calculates  and return the overlap
def calculateOverlap(list1,list2):
    a=0
    for i in list1:
        if (i in list2):
            a+=1
    overlap=a/((len(list1)+ len(list2)-a)*1.0)
    return overlap
#return the third element of the given argument
def getKey(item):
    return item[2]

# Compare all the subreddits  pairwise and copy them and the overlapping rate into a temporary list
tempList1=[]
for i in range(len(subredditList)):
    for j in range(i+1,len(subredditList)):
        #copy the pair of subreddits and the overlap into a variable which is then added to a temporary list.
        a =[subredditList[i],subredditList[j], calculateOverlap(clearModSet[i],clearModSet[j])]
        tempList1.append(a)
#create another temporary list using the elements of tempList1 in a descending order by the value of overlap.
tempList2=sorted(tempList1,key=getKey,reverse=True)

# create a table,Copy the 50 highest ranking pairwise overlaps from the  tempList2 into this table and print it
overlapTable =pt.PrettyTable(["Subreddit 1", "Subreddit 2", "Degree of Overlap"])
for i in range(50):
    overlapTable.add_row(tempList2[i])
print overlapTable

# Get the index of subreddit 'The_Donald 
indexDonald= subredditList.index('The_Donald')
# comb through clearModList and find moderator lists that share common moderators with the moderator list of The_Donald
tempTabDonald=[]
for i in range(len(clearModList)):
    listDonald=[]
    if(i!=indexDonald):
        #find and copy all the common moderators with The_Donald and their subreddits into a temporary list
        for j in range(len(clearModList[i])):
            if(clearModList[i][j] in clearModList[indexDonald]):
                listDonald.append(clearModList[i][j])
        if(len(listDonald)>0):
            a=[subredditList[i],listDonald]
            tempTabDonald.append(a)
#create a table, copy the elements of tempTabDonald and print the table   
tabDonald = pt.PrettyTable(["Subreddit", "common moderators with The_Donald"])
for i in tempTabDonald:
    tabDonald.add_row(i)
print tabDonald
#create two lists to save the link and comment karma
linkKarmaList=[]
commentKarmaList=[]

for i in clearModList:
    #variables for counting the number of valid karma as well as for the sums of link karma and comment karma
    numberofKarma=0
    linkkarma_sum=0
    commentkarma_sum=0
    for j in i:
        try:
            a= reddit.redditor("{}".format(j))
            linkkarma_sum+=a.link_karma
            commentkarma_sum+=a.comment_karma
            numberofKarma+=1
        # In case we, for some reason, can't get a given subredditor's karma we continue with the next subredditor in the list
        except Exception as e:
            continue 
    #divide the sums of link and comment karma by numberofKarma for each moderator list, copy in a variable and append
    commentKarmaList.append(commentkarma_sum/(numberofKarma*1.0))
    linkKarmaList.append(linkkarma_sum/(numberofKarma*1.0))
print 'Karma lists are ready.'


# In[133]:

# Set the size of the plot
plt.rcParams["figure.figsize"] =[17,17]
f=plt.figure()
# create a scatter plot with the data from commentKarmaList an linkKarmaList
for i in range (len(commentKarmaList)):
    plt.scatter(commentKarmaList[i], linkKarmaList[i], marker="o", s=float(subscriberDict[i])*0.0001)
    
# Create a linear regression
plt.plot(ny.unique(commentKarmaList), ny.poly1d(ny.polyfit(commentKarmaList, linkKarmaList, 1))(ny.unique(commentKarmaList)))

# limit the range of x and y axis and create more space for the visualisation of the markers
plt.xlim(500)
plt.ylim(1500)
plt.xlabel('Comment Karma ')
plt.ylabel('Link Karma')
plt.title('Link and comment Karma of most popular Forums on reddit')
plt.xscale('log')
plt.yscale('log')
plt.legend()
# put name labels on the markers of subreddits with more than 1000000 subredditors
for i in subscriberDict:
    if(len(subscriberDict[i])>6):
        plt.annotate(subredditList[i], (commentKarmaList[i],linkKarmaList[i]),clip_on=True)
plt.show
# Create a PDF-File with the graphic
f.savefig("LOESUNG-Projekt-Treijner-2.pdf", bbox_inches='tight')


# In[ ]:



