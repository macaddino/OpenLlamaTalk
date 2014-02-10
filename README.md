OpenLlamaTalk
=============

OpenLlamaTalk, A Google Glass App for Language Comprehension &amp; Learning

Welcome to OpenLlamaTalk! The purpose of this app is to detect, correct,
and visualize informal language usage in speech.

For our purposes, informal language consists of
a) Grammatical errors
b) Vocal fillers and interjections

The user will be provided with a daily score of his or her quality of
speech.

I strongly recommend looking at Google's official documentation - https://developers.google.com/glass/develop/gdk/

[Changelog]

--
02/10/2014
-First commit.

Voice trigger is "Hello Glass" and it displays a card to say "TAP TO 
RECORD SPEECH". Upon tapping, the user will see a card which displays
"TAPPED GLASS" and Android Speech to Text will be activated. Following
the Speech to Text transcription, the user can tap again to activate
Speech to Text once more.

See src/com/openllamatalk/helloglass/magic.java for more commentary and documentation.

--
