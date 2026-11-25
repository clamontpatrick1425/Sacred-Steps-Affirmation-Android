package com.example.data.local

object AffirmationSeedData {

    private data class MonthTheme(
        val monthNumber: Int,
        val monthName: String,
        val daysCount: Int,
        val titles: List<String>,
        val scriptures: List<String>,
        val truthReframes: List<String>,
        val affirmations: List<String>,
        val practices: List<String>,
        val prompts: List<String>
    )

    private data class DayOverride(
        val title: String,
        val scripture: String,
        val truthReframe: String,
        val affirmationText: String,
        val practiceAction: String,
        val reflectionPrompt: String
    )

    private val featuredDays: Map<Int, DayOverride> = mapOf(
        1 to DayOverride(
            title = "You Do Not Have to Carry It Alone",
            scripture = "\"Come to me, all you who are weary and burdened, and I will give you rest.\" — Matthew 11:28",
            truthReframe = "The lie says I have to fix myself before God or anyone else will welcome me. The truth is grace meets me right in the middle of my tiredness.",
            affirmationText = "Today, I lay down the weight I was never meant to hold by myself. One honest step is enough.",
            practiceAction = "Take three slow breaths and tell God out loud what feels heaviest right now.",
            reflectionPrompt = "What burden are you ready to stop carrying alone today?"
        ),
        2 to DayOverride(
            title = "Honesty Is Where Healing Starts",
            scripture = "\"Surely you desire integrity in the inner parts.\" — Psalm 51:6",
            truthReframe = "Shame whispers that hiding keeps me safe. Truth reminds me that honest light is where real freedom begins.",
            affirmationText = "I do not need to pretend I am okay when I am hurting. My honesty is welcome here.",
            practiceAction = "Write down one sentence today that is completely true, without editing it to sound stronger.",
            reflectionPrompt = "Where have you been pretending to be fine when your heart needs care?"
        ),
        3 to DayOverride(
            title = "Morning Mercies Are Always Fresh",
            scripture = "\"His compassions never fail. They are new every morning.\" — Lamentations 3:22-23",
            truthReframe = "Yesterday's stumble does not lock me out of today's mercy. Every sunrise brings a clean invitation to begin again.",
            affirmationText = "I am not defined by my worst day. Fresh grace is waiting for me right here.",
            practiceAction = "Look out a window for thirty seconds and quietly say, \"Welcome to a new morning.\"",
            reflectionPrompt = "What mistake from the past are you ready to hand back to yesterday?"
        ),
        4 to DayOverride(
            title = "Learning to Trust One Minute at a Time",
            scripture = "\"My grace is sufficient for you, for my power is made perfect in weakness.\" — 2 Corinthians 12:9",
            truthReframe = "Fear tells me I have to figure out the next five years tonight. Grace only asks me to walk through this next hour.",
            affirmationText = "I do not need strength for next month. I have enough grace for this moment.",
            practiceAction = "Whenever your mind races into next week, gently bring your hands together and return to this single hour.",
            reflectionPrompt = "What future worry is stealing your peace right now?"
        ),
        5 to DayOverride(
            title = "Surrender Is Not Defeat",
            scripture = "\"Trust in the Lord with all your heart and lean not on your own understanding.\" — Proverbs 3:5",
            truthReframe = "Control feels like safety, yet it wears me down. Surrender is opening my clenched fists so peace can finally land.",
            affirmationText = "Letting go is not losing. It is trusting that I am held even when I cannot steer everything.",
            practiceAction = "Unclench your jaw, drop your shoulders, and open your palms upward for ten quiet seconds.",
            reflectionPrompt = "What outcome are you gripping tightly that you could loosen your hold on today?"
        ),
        6 to DayOverride(
            title = "Safe People Make the Road Lighter",
            scripture = "\"Two are better than one, because they have a good return for their labor.\" — Ecclesiastes 4:9",
            truthReframe = "Isolation tells me nobody would understand my story. Community proves that shared truth shrinks shame.",
            affirmationText = "I was built for honest connection. Reaching out is a sign of courage, not weakness.",
            practiceAction = "Send a short, honest text to a sponsor, friend, or mentor—even just to say \"Thinking of you today.\"",
            reflectionPrompt = "Who is one safe person you can let a little closer this week?"
        ),
        7 to DayOverride(
            title = "A Quiet Celebration of Staying Present",
            scripture = "\"He who began a good work in you will carry it on to completion.\" — Philippians 1:6",
            truthReframe = "Perfectionism says small steps do not count. Grace celebrates every single time I chose honesty over hiding.",
            affirmationText = "Every quiet, faithful step I took this week matters. God sees the roots growing beneath the surface.",
            practiceAction = "Pause and name one small moment this week when you chose care instead of self-destruction.",
            reflectionPrompt = "Look back over these first seven days. What feels even slightly softer in you?"
        ),
        8 to DayOverride(
            title = "Stepping Past What Used to Hold Me",
            scripture = "\"Forget the former things; do not dwell on the past. See, I am doing a new thing.\" — Isaiah 43:18-19",
            truthReframe = "Guilt wants me to keep staring into the rearview mirror. Redemption asks me to notice the road opening in front of me.",
            affirmationText = "My past taught me why I need grace, but it does not own my tomorrow.",
            practiceAction = "Drink a full glass of water slowly and thank your body for carrying you into a new week.",
            reflectionPrompt = "What old label are you leaving behind as you step into Week 2?"
        ),
        30 to DayOverride(
            title = "Thirty Days of Choosing Light",
            scripture = "\"The light shines in the darkness, and the darkness has not overcome it.\" — John 1:5",
            truthReframe = "A month of real recovery is rarely loud or glamorous. It is built out of ordinary days where I kept showing up.",
            affirmationText = "I honor the quiet courage it took to reach this page. Grace has met me here.",
            practiceAction = "Read back through one journal note from earlier this month and notice how you kept going.",
            reflectionPrompt = "What quiet change in your heart deserves to be celebrated today?"
        ),
        90 to DayOverride(
            title = "Rooted Deeper Than the Storm",
            scripture = "\"They will be like a tree planted by the water that sends out its roots by the stream.\" — Jeremiah 17:8",
            truthReframe = "Ninety days does not mean life stops testing me. It means my roots are learning where to find steady water.",
            affirmationText = "I am no longer only surviving the storm. I am growing roots that hold.",
            practiceAction = "Step outside for two minutes today and notice something living that grew quietly over time.",
            reflectionPrompt = "What daily practice has helped ground you most over these ninety days?"
        ),
        180 to DayOverride(
            title = "Halfway Through the Year in Grace",
            scripture = "\"You crown the year with your bounty, and your carts overflow with abundance.\" — Psalm 65:11",
            truthReframe = "Looking back can stir both gratitude and tender grief for lost years. Grace holds room for both without rushing me.",
            affirmationText = "I am halfway through this year, and I am still here—honest, learning, and deeply loved.",
            practiceAction = "Write a short note of thanks to yourself for not giving up on hard mornings.",
            reflectionPrompt = "If you could speak gently to yourself six months ago, what would you say?"
        ),
        318 to DayOverride(
            title = "Gratitude for the People Who Stayed",
            scripture = "\"A friend loves at all times, and a brother is born for a time of adversity.\" — Proverbs 17:17",
            truthReframe = "Shame told me I was too broken to stay beside. Yet grace placed steady hands around me when I could not stand alone.",
            affirmationText = "I give thanks for every voice that answered the phone, prayed in quiet, and believed in my redemption.",
            practiceAction = "Reach out to someone who stood with you during a hard season and say a simple, sincere thank you.",
            reflectionPrompt = "Who stayed beside you when things were messy, and what would you like them to know today?"
        ),
        365 to DayOverride(
            title = "Carrying the Light Forward",
            scripture = "\"Therefore, if anyone is in Christ, the new creation has come.\" — 2 Corinthians 5:17",
            truthReframe = "Day 365 is not a finish line where I stop needing grace. It is the day I look back and realize I am walking in new life.",
            affirmationText = "I am a new creation, still growing, still honest, and ready to share the hope that saved me.",
            practiceAction = "Pray for one person who feels today the way you felt on Day 1.",
            reflectionPrompt = "Looking across this whole year, what does grace mean to you now?"
        )
    )

    private val months = listOf(
        MonthTheme(
            monthNumber = 1,
            monthName = "January",
            daysCount = 31,
            titles = listOf(
                "A Gentle Beginning", "Permission to Rest", "Telling the Whole Truth",
                "Small Faith Still Counts", "When Old Cravings Speak", "Safe Harbor in Silence",
                "Putting Down the Mask", "Strength for Just Today"
            ),
            scriptures = listOf(
                "\"The Lord is near to the brokenhearted.\" — Psalm 34:18",
                "\"In quietness and trust is your strength.\" — Isaiah 30:15",
                "\"I will give you a new heart and put a new spirit in you.\" — Ezekiel 36:26",
                "\"The Lord is my shepherd, I lack nothing.\" — Psalm 23:1"
            ),
            truthReframes = listOf(
                "Struggling does not mean I am failing. It means I am finally awake enough to heal.",
                "I do not have to earn God's patience with perfection. Patience is already how He looks at me.",
                "Feeling tempted is not a moral collapse. How I answer in the next quiet minute is where grace lives.",
                "Starting small is not weak. Every strong bridge begins with one steady stone."
            ),
            affirmations = listOf(
                "Today I choose honesty over appearance, and peace over panic.",
                "I am safe to slow down and let grace catch up with my racing thoughts.",
                "I do not walk into this day unguarded or unseen. I am carried.",
                "My worth did not vanish in my broken chapters. I am still redeemable."
            ),
            practices = listOf(
                "Sit quietly for sixty seconds before checking your phone and breathe slowly.",
                "Text one trusted friend a simple word about how your morning feels.",
                "Step away from one noisy trigger today and choose a calm walk or glass of water.",
                "Write three honest words on paper describing what your soul needs most right now."
            ),
            prompts = listOf(
                "What truth about God's patience feels hardest—and sweetest—to believe today?",
                "Where did you notice tension in your body this morning, and what might it be telling you?",
                "What small victory today would you normally overlook?",
                "If shame spoke to you this week, what truer sentence can answer it?"
            )
        ),
        MonthTheme(
            monthNumber = 2,
            monthName = "February",
            daysCount = 28,
            titles = listOf(
                "Hope Beneath the Surface", "You Are Not Too Late", "Softening Hard Places",
                "Learning to Wait Without Fear", "Held Through the Fog", "Choosing Kindness Toward Yourself",
                "When Feelings Rise Fast"
            ),
            scriptures = listOf(
                "\"Be still, and know that I am God.\" — Psalm 46:10",
                "\"Weeping may stay for the night, but rejoicing comes in the morning.\" — Psalm 30:5",
                "\"Cast all your anxiety on him because he cares for you.\" — 1 Peter 5:7",
                "\"Hope does not put us to shame.\" — Romans 5:5"
            ),
            truthReframes = listOf(
                "When I cannot feel progress, roots are still growing underground.",
                "Disappointment does not cancel the promise of renewal. It asks me to lean closer.",
                "Harsh self-talk never healed anyone. Gentleness gives truth room to stay.",
                "I am allowed to feel sadness without reaching for old ways to numb it."
            ),
            affirmations = listOf(
                "Hope is quietly at work in me, even on ordinary gray mornings.",
                "I treat myself today with the same mercy I offer a hurting friend.",
                "Feelings are weather passing through; grace is the ground beneath my feet.",
                "I am learning to trust a steady pace instead of rushing toward exhaustion."
            ),
            practices = listOf(
                "Place a hand over your heart and speak one kind sentence to yourself aloud.",
                "Eat one unhurried meal today without screens.",
                "Pause before replying when frustration rises; take one deep breath first.",
                "Name two things that brought quiet comfort in the last twenty-four hours."
            ),
            prompts = listOf(
                "Where do you need a softer tone with yourself today?",
                "What emotion feels easiest to avoid, and what happens when you simply name it?",
                "When did you last notice a quiet spark of hope?",
                "What helps you feel anchored when the day feels foggy?"
            )
        ),
        MonthTheme(
            monthNumber = 3,
            monthName = "March",
            daysCount = 31,
            titles = listOf(
                "Stepping Into the Light", "Clearing Out Old Excuses", "Courage to Be Known",
                "Breaking the Silence", "Truth Without Condemnation", "Honest Inventory",
                "Making Room for Peace"
            ),
            scriptures = listOf(
                "\"Walk as children of light.\" — Ephesians 5:8",
                "\"If we walk in the light, as he is in the light, we have fellowship with one another.\" — 1 John 1:7",
                "\"Create in me a pure heart, O God.\" — Psalm 51:10",
                "\"Where the Spirit of the Lord is, there is freedom.\" — 2 Corinthians 3:17"
            ),
            truthReframes = listOf(
                "Secrets keep old patterns fed. Honest conversation cuts off their oxygen.",
                "Self-reflection is not self-punishment; it is cleaning a window so sunlight can enter.",
                "Admitting a flaw does not shrink my worth—it deepens my freedom.",
                "God already knows my full story and still chose to stay near."
            ),
            affirmations = listOf(
                "I step into the light without fear, knowing grace covers what I uncover.",
                "I can face what is true about me without hating myself.",
                "Honesty sets my feet on solid ground.",
                "I release the exhausting job of keeping up appearances."
            ),
            practices = listOf(
                "Share one honest thought with your sponsor or accountability partner today.",
                "Tidy one small corner of your room as an act of inner care.",
                "Ask God to show you one honest habit ready for renewal.",
                "Speak truthfully in every small conversation today, even when it feels awkward."
            ),
            prompts = listOf(
                "What truth about your past feels less scary when you say it in the light?",
                "What distraction keeps you from sitting quietly with your own heart?",
                "How does it feel to know God does not flinch at your honesty?",
                "What one clear boundary would protect your peace this week?"
            )
        ),
        MonthTheme(
            monthNumber = 4,
            monthName = "April",
            daysCount = 30,
            titles = listOf(
                "Open Hands", "Releasing What I Cannot Control", "New Life After Winter",
                "The Relief of Letting Go", "Trusting the Unseen Work", "Grace for Unanswered Questions",
                "Breathing Room"
            ),
            scriptures = listOf(
                "\"Cast your cares on the Lord and he will sustain you.\" — Psalm 55:22",
                "\"See! The winter is past; the rains are over and gone.\" — Song of Songs 2:11",
                "\"Peace I leave with you; my peace I give you.\" — John 14:27",
                "\"The Lord will fight for you; you need only to be still.\" — Exodus 14:14"
            ),
            truthReframes = listOf(
                "Trying to control everyone's opinion leaves me empty. Peace grows when I focus on my own next step.",
                "Spring does not force flowers open; it gives them sun and rain. Recovery grows the same way.",
                "I do not have to resolve every question before taking today's faithful action.",
                "Letting go of old bitterness frees my hands to receive new joy."
            ),
            affirmations = listOf(
                "I open my hands and release what was never mine to manage.",
                "New life is quietly sprouting in places I thought were finished.",
                "I welcome peace even while some answers are still unfolding.",
                "Today I choose trust over tight control."
            ),
            practices = listOf(
                "Write down one worry on paper, fold it, and set it aside for the afternoon.",
                "Notice three signs of renewal around you today—light, leaves, breath, kindness.",
                "Turn off notifications for one quiet hour tonight.",
                "Practice saying \"I do not have to fix that today\" when anxiety knocks."
            ),
            prompts = listOf(
                "What situation are you trying to force that needs patience instead?",
                "Where do you sense new life beginning to stir in you?",
                "What resentment feels ready to loosen its grip?",
                "How can you give yourself extra breathing room today?"
            )
        ),
        MonthTheme(
            monthNumber = 5,
            monthName = "May",
            daysCount = 31,
            titles = listOf(
                "Courage in Ordinary Moments", "Standing Steady", "Choosing the Next Right Thing",
                "When Doubt Whispers", "Truth Stronger Than Fear", "Faithful Presence",
                "Building Quiet Confidence"
            ),
            scriptures = listOf(
                "\"Be strong and courageous. Do not be afraid; for the Lord your God is with you.\" — Joshua 1:9",
                "\"God gave us a spirit not of fear but of power and love and self-control.\" — 2 Timothy 1:7",
                "\"He gives strength to the weary and increases the power of the weak.\" — Isaiah 40:29",
                "\"The Lord is my light and my salvation—whom shall I fear?\" — Psalm 27:1"
            ),
            truthReframes = listOf(
                "Courage does not mean my hands never shake. It means I take the honest step anyway.",
                "Fear exaggerates the size of obstacles and hides the strength walking beside me.",
                "Small repeated choices reshape my life far more than dramatic promises.",
                "I do not need everyone's applause to know I am walking in integrity."
            ),
            affirmations = listOf(
                "I have quiet courage for the next honest conversation and the next right choice.",
                "Fear visits my mind, but it does not own my decisions.",
                "I am learning to stand steady without numbing what hurts.",
                "Each day of honesty builds a stronger foundation beneath me."
            ),
            practices = listOf(
                "Complete one small task you have been putting off out of anxiety.",
                "Say no politely to one commitment that drains your recovery.",
                "Stand tall, breathe deeply, and step outside into fresh air for five minutes.",
                "Encourage someone else quietly today with a kind word."
            ),
            prompts = listOf(
                "What small act of courage are you proud of from recent days?",
                "Where does fear try to talk you out of peace?",
                "What is the next right thing waiting gently in front of you?",
                "How has your confidence changed since Day 1?"
            )
        ),
        MonthTheme(
            monthNumber = 6,
            monthName = "June",
            daysCount = 30,
            titles = listOf(
                "Separating Guilt From Shame", "No Longer Defined by Mistakes", "Mercy Speaks Louder",
                "Healing the Inner Voice", "Loved Without Conditions", "Leaving the Shadows",
                "Beloved and Forgiven"
            ),
            scriptures = listOf(
                "\"There is now no condemnation for those who are in Christ Jesus.\" — Romans 8:1",
                "\"As far as the east is from the west, so far has he removed our transgressions.\" — Psalm 103:12",
                "\"You are precious and honored in my sight, and I love you.\" — Isaiah 43:4",
                "\"The Lord your God is with you... He will quiet you with his love.\" — Zephaniah 3:17"
            ),
            truthReframes = listOf(
                "Guilt says I made a mistake that can be repaired. Shame lies and says I am the mistake.",
                "Condemnation drives me back toward hiding; conviction gently calls me home.",
                "I cannot heal a heart by beating it up. Grace changes me faster than guilt ever could.",
                "My past chapters explain my need for mercy, not my permanent identity."
            ),
            affirmations = listOf(
                "I am not my worst decision. I am a beloved child walking toward light.",
                "Shame has no final say over my name or my future.",
                "I receive forgiveness without adding extra penance to what grace already paid.",
                "My story is still being redeemed."
            ),
            practices = listOf(
                "Catch one harsh thought about yourself today and rewrite it with compassion.",
                "Remind yourself gently: \"That was then. Today I am learning a new way.\"",
                "Take five slow minutes to journal without judging a single word.",
                "Spend time in a place or with a person where you feel genuinely accepted."
            ),
            prompts = listOf(
                "What lie of shame has lingered longest, and what truth replaces it?",
                "How would you comfort someone you love if they carried your guilt?",
                "What helps you tell the difference between healthy humility and toxic shame?",
                "Where do you feel mercy quieting old accusations?"
            )
        ),
        MonthTheme(
            monthNumber = 7,
            monthName = "July",
            daysCount = 31,
            titles = listOf(
                "Making Peace Where I Can", "Humble Repair", "Wisdom in Boundaries",
                "Forgiving What Hurt", "Integrity in Small Things", "Clearing the Air",
                "Walking Gently With Others"
            ),
            scriptures = listOf(
                "\"If it is possible, as far as it depends on you, live at peace with everyone.\" — Romans 12:18",
                "\"Be kind and compassionate to one another, forgiving each other.\" — Ephesians 4:32",
                "\"Blessed are the peacemakers.\" — Matthew 5:9",
                "\"Above all, clothe yourselves with love, which binds everything together.\" — Colossians 3:14"
            ),
            truthReframes = listOf(
                "Making amends is about taking honest responsibility for my part, not forcing someone else's timeline.",
                "Healthy boundaries are not walls of anger; they are doors that protect healing.",
                "Forgiving does not mean pretending a wound never hurt. It means refusing to let bitterness run my life.",
                "Living cleanly today is one of the deepest amends I can offer."
            ),
            affirmations = listOf(
                "I take responsibility with humility, and I release what I cannot fix alone.",
                "My words and actions today align with the person I am becoming.",
                "I can love others well while still honoring safe boundaries.",
                "Peace begins in how I show up right now."
            ),
            practices = listOf(
                "Pray for someone you once hurt—or someone who hurt you—asking for peace for both of you.",
                "Consult your sponsor before making any sensitive repair or outreach.",
                "Keep one small promise you made to yourself or someone else today.",
                "Listen fully to someone today without planning your defense."
            ),
            prompts = listOf(
                "Where is wisdom asking you to move slowly with amends?",
                "What boundary has helped keep your relationships healthier?",
                "Is there someone you need to forgive—including yourself?",
                "How does living in alignment bring peace to your evenings?"
            )
        ),
        MonthTheme(
            monthNumber = 8,
            monthName = "August",
            daysCount = 31,
            titles = listOf(
                "Steady on Ordinary Days", "Faithfulness Without Fanfare", "Guarding Your Peace",
                "Rhythms That Hold", "When Progress Feels Quiet", "Protecting Your Morning",
                "Daily Bread"
            ),
            scriptures = listOf(
                "\"Let us not become weary in doing good, for at the proper time we will reap a harvest.\" — Galatians 6:9",
                "\"Give us today our daily bread.\" — Matthew 6:11",
                "\"Above all else, guard your heart, for everything you do flows from it.\" — Proverbs 4:23",
                "\"Whoever can be trusted with very little can also be trusted with much.\" — Luke 16:10"
            ),
            truthReframes = listOf(
                "Recovery is protected in daily routines long before a crisis arrives.",
                "Boredom does not mean nothing is happening; stability is a gift I once prayed for.",
                "I do not have to chase big emotional highs to know I am growing.",
                "Taking care of sleep, meals, and prayer is deeply spiritual work."
            ),
            affirmations = listOf(
                "I welcome quiet, steady rhythms that keep my heart anchored.",
                "Ordinary faithfulness is building an extraordinary life.",
                "I protect my peace like something sacred, because it is.",
                "Today's small habits are caring for tomorrow's me."
            ),
            practices = listOf(
                "Check HALT today: notice if you are Hungry, Angry, Lonely, or Tired before reacting.",
                "Protect twenty minutes of quiet rest tonight.",
                "Drink water, stretch your shoulders, and thank God for an ordinary day.",
                "Write down one healthy habit that has become easier over the last month."
            ),
            prompts = listOf(
                "Which daily routine steadies you most when life speeds up?",
                "How do you handle quiet days without looking for drama or distraction?",
                "What warning sign tells you that you need rest earlier?",
                "What simple gift from today do you want to remember?"
            )
        ),
        MonthTheme(
            monthNumber = 9,
            monthName = "September",
            daysCount = 30,
            titles = listOf(
                "Mercy Over Perfection", "Returning Without Shame", "The Beauty of Second Chances",
                "Gentle Correction", "Held When I Stumble", "Learning From Every Season",
                "Softened by Grace"
            ),
            scriptures = listOf(
                "\"Though the righteous fall seven times, they rise again.\" — Proverbs 24:16",
                "\"The Lord is compassionate and gracious, slow to anger, abounding in love.\" — Psalm 103:8",
                "\"My soul finds rest in God alone.\" — Psalm 62:1",
                "\"He restores my soul.\" — Psalm 23:3"
            ),
            truthReframes = listOf(
                "If I miss a step or feel off balance, shame says stay down. Grace says take my hand and stand back up.",
                "Growth is not a straight upward line; it is a spiral that keeps returning closer to peace.",
                "Perfection was never the goal—honesty and willingness are.",
                "God's kindness leads me home far faster than fear ever could."
            ),
            affirmations = listOf(
                "Whenever I drift, I can return immediately without shame.",
                "Grace welcomes me back before I even finish apologizing.",
                "I am a student of peace, learning day by day.",
                "My slips do not erase my sincerity."
            ),
            practices = listOf(
                "If you missed a day of reading or journaling, say aloud: \"Welcome back. Today is fresh.\"",
                "Do an honest, kind two-minute evening review before sleep.",
                "Offer someone patience today when they make a small mistake.",
                "Release one impossible standard you set for yourself this week."
            ),
            prompts = listOf(
                "How do you usually talk to yourself after an imperfect day?",
                "What would change if you believed grace genuinely enjoys welcoming you back?",
                "What lesson from a past stumble has protected you since?",
                "Where do you feel your soul being restored?"
            )
        ),
        MonthTheme(
            monthNumber = 10,
            monthName = "October",
            daysCount = 31,
            titles = listOf(
                "Listening in the Quiet", "Prayer Without Performance", "Still Waters",
                "Knowing You Are Heard", "Guidance for Today", "Quiet Trust",
                "Making Space to Hear"
            ),
            scriptures = listOf(
                "\"Speak, for your servant is listening.\" — 1 Samuel 3:10",
                "\"He leads me beside quiet waters.\" — Psalm 23:2",
                "\"Call to me and I will answer you.\" — Jeremiah 33:3",
                "\"Draw near to God, and he will draw near to you.\" — James 4:8"
            ),
            truthReframes = listOf(
                "Prayer does not require polished phrases. Honest sighs and plain words reach heaven just as fast.",
                "Silence is not emptiness; it is where I finally hear what my soul has been trying to say.",
                "Direction usually arrives for the next step, not the whole map.",
                "God is never too busy or tired to listen to me."
            ),
            affirmations = listOf(
                "My simple, unfiltered prayers are heard and treasured.",
                "I slow down enough today to notice quiet guidance.",
                "I do not have to perform spirituality to be close to God.",
                "In stillness, clarity gently finds me."
            ),
            practices = listOf(
                "Spend three silent minutes today with no agenda except listening.",
                "Pray one honest, one-sentence prayer in the middle of your busiest hour.",
                "Take a brief walk without headphones and notice the sounds around you.",
                "Write down one question you are sitting with and leave space for stillness."
            ),
            prompts = listOf(
                "What does prayer feel like when you drop all performance?",
                "When is it easiest for you to feel genuinely still?",
                "What quiet nudge have you been sensing lately?",
                "What makes it hard to pause and listen during busy weeks?"
            )
        ),
        MonthTheme(
            monthNumber = 11,
            monthName = "November",
            daysCount = 30,
            titles = listOf(
                "Gratitude for Small Mercies", "People Who Held the Rope", "Noticing the Good",
                "Thankfulness in Hard Seasons", "Shared Strength", "Grace Around the Table",
                "Counting Quiet Blessings"
            ),
            scriptures = listOf(
                "\"Give thanks in all circumstances.\" — 1 Thessalonians 5:18",
                "\"I thank my God every time I remember you.\" — Philippians 1:3",
                "\"Every good and perfect gift is from above.\" — James 1:17",
                "\"The Lord has done great things for us, and we are filled with joy.\" — Psalm 126:3"
            ),
            truthReframes = listOf(
                "Gratitude does not deny pain; it refuses to let pain be the only thing I see.",
                "Nobody heals completely on their own. Thankfulness connects me back to the hands that helped.",
                "Even small mercies—a clear morning, an honest friend, a calm breath—are real gifts.",
                "Joy grows naturally where thankfulness is practiced."
            ),
            affirmations = listOf(
                "My eyes are open to the quiet mercies woven through today.",
                "I carry deep gratitude for the people and grace that walked me here.",
                "Thankfulness shifts my focus from what I lack to how I am held.",
                "I celebrate every honest step of healing."
            ),
            practices = listOf(
                "Name three ordinary gifts from today before going to sleep.",
                "Thank someone specifically for one way they supported your walk.",
                "Share encouragement in a group or with a friend who feels discouraged.",
                "Pause before your next meal or coffee and say a quiet word of thanks."
            ),
            prompts = listOf(
                "Who helped hold the rope for you when you could not climb alone?",
                "What ordinary blessing today would have felt impossible a year ago?",
                "How does gratitude soften anxious thoughts for you?",
                "What part of your recovery journey are you most thankful for right now?"
            )
        ),
        MonthTheme(
            monthNumber = 12,
            monthName = "December",
            daysCount = 31,
            titles = listOf(
                "New Creation", "Light That Cannot Be Put Out", "Carrying Hope to Others",
                "Celebration Without Shame", "Peace on the Horizon", "Stepping Into Purpose",
                "Redeemed and Still Growing"
            ),
            scriptures = listOf(
                "\"Therefore, if anyone is in Christ, the new creation has come: The old has gone, the new is here!\" — 2 Corinthians 5:17",
                "\"The people walking in darkness have seen a great light.\" — Isaiah 9:2",
                "\"You are the light of the world.\" — Matthew 5:14",
                "\"He gives us the victory through our Lord Jesus Christ.\" — 1 Corinthians 15:57"
            ),
            truthReframes = listOf(
                "My healed places are becoming lanterns for someone else walking through the dark.",
                "Growth never asks me to forget grace—it invites me to pass grace along.",
                "I am not merely managing an old problem; I am stepping into a new life.",
                "Every honest story of redemption reminds another person they are not hopeless."
            ),
            affirmations = listOf(
                "I walk as a new creation—grateful, grounded, and open to joy.",
                "My life carries quiet light into rooms that feel dark.",
                "I celebrate how far grace has brought me without fear of tomorrow.",
                "I am ready to share hope gently with whoever needs it next."
            ),
            practices = listOf(
                "Offer a listening ear or quiet kindness to someone starting out.",
                "Celebrate a milestone—large or small—with a warm meal or grateful prayer.",
                "Write down three truths you want to carry into the coming year.",
                "Rest well tonight knowing you do not walk into tomorrow alone."
            ),
            prompts = listOf(
                "What feels brand new in your heart compared to when you started?",
                "How has your story already encouraged someone else, or how might it?",
                "What truth from this year do you want to anchor next year?",
                "Take a moment to celebrate: what are you genuinely proud of today?"
            )
        )
    )

    fun buildFirstSevenDays(): List<AffirmationEntity> {
        return (1..7).map { day ->
            val override = featuredDays.getValue(day)
            AffirmationEntity(
                dayNumber = day,
                weekNumber = 1,
                monthNumber = 1,
                monthName = "January",
                title = override.title,
                scripture = override.scripture,
                truthReframe = override.truthReframe,
                affirmationText = override.affirmationText,
                practiceAction = override.practiceAction,
                reflectionPrompt = override.reflectionPrompt,
                isCompleted = false
            )
        }
    }

    fun buildAll365Days(): List<AffirmationEntity> {
        val result = ArrayList<AffirmationEntity>(365)
        var currentDay = 1

        for (month in months) {
            for (dayInMonth in 1..month.daysCount) {
                val weekNumber = ((currentDay - 1) / 7 + 1).coerceIn(1, 52)
                val override = featuredDays[currentDay]

                val entity = if (override != null) {
                    AffirmationEntity(
                        dayNumber = currentDay,
                        weekNumber = weekNumber,
                        monthNumber = month.monthNumber,
                        monthName = month.monthName,
                        title = override.title,
                        scripture = override.scripture,
                        truthReframe = override.truthReframe,
                        affirmationText = override.affirmationText,
                        practiceAction = override.practiceAction,
                        reflectionPrompt = override.reflectionPrompt,
                        isCompleted = false
                    )
                } else {
                    val idx = dayInMonth - 1
                    val titleBase = month.titles[idx % month.titles.size]
                    val title = if (idx < month.titles.size) {
                        titleBase
                    } else {
                        "$titleBase · ${month.monthName} $dayInMonth"
                    }
                    AffirmationEntity(
                        dayNumber = currentDay,
                        weekNumber = weekNumber,
                        monthNumber = month.monthNumber,
                        monthName = month.monthName,
                        title = title,
                        scripture = month.scriptures[idx % month.scriptures.size],
                        truthReframe = month.truthReframes[(idx + 1) % month.truthReframes.size],
                        affirmationText = month.affirmations[(idx + 2) % month.affirmations.size],
                        practiceAction = month.practices[(idx + 3) % month.practices.size],
                        reflectionPrompt = month.prompts[idx % month.prompts.size],
                        isCompleted = false
                    )
                }

                result.add(entity)
                currentDay++
            }
        }
        return result
    }
}
