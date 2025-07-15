ChordSymbol and NoteSymbol

Simple notation for chords and notes in SuperCollider

⸻

Features
  •	Single-note frequency with .asFreq on Symbols → e.g. \c4.asFreq
  •	Single-chord frequencies with .asFreqs on Symbols → e.g. \Cmaj7.asFreqs
  •	Note frequencies with .noteProgFreq → returns frequency arrays via .midicps
  •	Note sequences with .noteProg → returns MIDI note arrays
  •	Chord frequencies with .chordProgFreqs → returns frequency arrays via .midicps
	•	Chord progressions with .chordProg → returns MIDI note arrays





⸻

Underscore Syntax for Chords

You can now specify octave and duration in chord symbols:
	•	First underscore (_) → octave (single digit)
	•	Second underscore (_) → duration (as in traditional slash notation duration)
	•	Examples:

\Cm7          // C minor 7 in default octave
\Cm7_4        // C minor 7 in octave 4
\Cm7_4_8      // C minor 7 in octave 4, eighth note duration
\Cm7__8       // C minor 7 in default octave, eighth note duration
\Cm7_d3_4     // C minor 7 over D\u266f (D#), octave 3, quarter note



Inversion is detected when the part after the chord name is not a single digit.

⸻

Chord Progressions

// MIDI note arrays
[ \Cm_eb, \Fm, \Gm, \Cm_g, \Cm_eb, \Fm, \Gm_d, \Cm ].chordProg;    
[ \C, \G_b, \F_a, \G_b ].chordProg;

// Frequency arrays
[ \Cm_eb, \Fm, \Gm, \Cm_g ].chordProgFreqs;


⸻

Note Sequences

// MIDI note arrays
[ \E4, \Fs4, \B4, \Cs5, \D5, \Fs4, \E4, \Cs5, \B4, \Fs4, \D5, \Cs5 ].noteProg;

// Frequency arrays
[ \E4, \Fs4, \B4 ].noteProgFreq;


⸻

Traditional Chord & Note Examples
	•	Chords (MIDI degrees):

\C.asNotes              // [0, 4, 7]
\Cmajor.asNotes         // [0, 4, 7]
\CM.asNotes             // [0, 4, 7]
\Cm.asNotes             // [0, 3, 7]
\Em7sharp5flat9.asNotes // [4, 7, 12, 14, 17]


	•	Sharps and flats:

\Gs.asNotes             // [8, 12, 15]
\Dbmaj11.asNotes        // [1, 5, 8, 12, 15, 18]


	•	Slash/inversions (use underscore instead of slash):

\C_g.asNotes            // [7, 12, 16]
\Fm_gs.asNotes          // [8, 12, 17]
\Dsus4_g.asNotes        // [7, 9, 14]



⸻

Degrees in a Scale

\Cmajor.asDegrees                  // [0, 2, 4]
\Cminor.asDegrees                  // [0, 1.1, 4]
\Cmajor.asDegrees(Scale.minor)     // major chord in minor mode -> [0, 2.1, 4]
\m7sharp5.asDegrees(Scale.dorian)  // [0, 2, 4.1, 6]

[ \Cm_eb, \Fm, \Gm ].chordProgDegrees(Scale.dorian);


⸻

Frequency Methods
	•	Single-chord frequencies:

\Cmaj7.asFreqs    // e.g. [261.63, 329.63, 392.00, 493.88]


	•	Single-note frequency:

\c4.asFreq        // e.g. 261.63


	•	Chord progression frequencies:

[ \C, \G, \Am ].chordProgFreqs;


	•	Note progression frequencies:

[ \c4, \e4, \g4 ].noteProgFreq;



⸻

Extending Shapes
	•	List built-in shapes: ChordSymbol.shapes.keys
	•	Add new shapes: ChordSymbol.shapes.put(\myShape, [0,2,5,7])

⸻

Check ChordSymbol and NoteSymbol help in SuperCollider for full API details.
