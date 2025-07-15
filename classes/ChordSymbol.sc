// Helper functions for describing chord using traditional language
ChordSymbol {
    // a list of traditional chord shapes
    classvar <shapes;

    *autoChord {
        this.preProcessor = { |code|
            "\\[:alnum:]*".findAllRegexp(code).postln;
        };
    }

    *initClass {
        shapes = (
            major:          [0, 4, 7],
            minor:          [0, 3, 7],
            major7:         [0, 4, 7, 11],
            dom7:           [0, 4, 7, 10],
            minor7:         [0, 3, 7, 10],
            aug:            [0, 4, 8],
            dim:            [0, 3, 6],
            dim7:           [0, 3, 6, 9],
            '1':            [0],
            '5':            [0, 7],
            plus:           [0, 4, 8],
            sharp5:         [0, 4, 8],
            msharp5:        [0, 3, 8],
            sus2:           [0, 2, 7],
            sus4:           [0, 5, 7],
            '6':            [0, 4, 7, 9],
            m6:             [0, 3, 7, 9],
            '7sus2':        [0, 2, 7, 10],
            '7sus4':        [0, 5, 7, 10],
            '7flat5':       [0, 4, 6, 10],
            m7flat5:        [0, 3, 6, 10],
            '7sharp5':      [0, 4, 8, 10],
            m7sharp5:       [0, 3, 8, 10],
            '9':            [0, 4, 7, 10, 14],
            m9:             [0, 3, 7, 10, 14],
            m7sharp9:       [0, 3, 7, 10, 14],
            maj9:           [0, 4, 7, 11, 14],
            '9sus4':        [0, 5, 7, 10, 14],
            '6by9':         [0, 4, 7, 9, 14],
            m6by9:          [0, 3, 9, 7, 14],
            '7flat9':       [0, 4, 7, 10, 13],
            m7flat9:        [0, 3, 7, 10, 13],
            '7flat10':      [0, 4, 7, 10, 15],
            '9sharp5':      [0, 1, 13],
            m9sharp5:       [0, 1, 14],
            '7sharp5flat9': [0, 4, 8, 10, 13],
            m7sharp5flat9:  [0, 3, 8, 10, 13],
            '11':           [0, 4, 7, 10, 14, 17],
            m11:            [0, 3, 7, 10, 14, 17],
            maj11:          [0, 4, 7, 11, 14, 17],
            '11sharp':      [0, 4, 7, 10, 14, 18],
            m11sharp:       [0, 3, 7, 10, 14, 18],
            '13':           [0, 4, 7, 10, 14, 17, 21],
            m13:            [0, 3, 7, 10, 14, 17, 21]
        );

        // set up some alternative names for shapes
        shapes.m = shapes.minor;
        shapes[\M] = shapes.major;
        shapes[\7] = shapes.dom7;
        shapes[\M7] = shapes.major7;
        shapes[\m7] = shapes.minor7;
        shapes.augmented = shapes.aug;
        shapes.diminished = shapes.dim;
        shapes.diminished7 = shapes.dim7
    }

    // converts chords in array to notes in an array
    *noteProgression { |array|
        ^array.collect { |c| this.asNotes(c) };
    }

    // converts chord names in array to degrees of scale in an array
    *degreeProgression { |array scale|
        ^array.collect { |c| this.asDegrees(c, scale) };
    }

    // outputs notes in a named chord to degrees of scale in an array
    *asDegrees { |input scale stepsPerOctave=12|
        // reguritate anything we definately can't process
        if(input.isRest or: (input.isKindOf(String) or: input.isKindOf(Symbol)).not) {
            ^input
        };

        ^ChordSymbol.asNotes(input).asArray.collect { |n|
            // TODO when next version of SC comes out use keyToDegree
            n.keyToDegree2(scale, stepsPerOctave);
        }
    }

    *asNotes { |input|
		var over, chord, shape, root = 0, noteNameLength = 0, dur, name, octave = 0, parts, octaveShift, potentialRoot, octavePart, durPart;

		// return early if not a valid input
		if(input.isRest or: (input.isKindOf(String) or: input.isKindOf(Symbol)).not) {
			^input
		};

		// convert to string and split by underscores (max 3 parts)
		name = input.asString;
		parts = name.split($\_);
		name = parts[0];
		octavePart = parts.size > 1.if({ parts[1] }, { nil });
		durPart    = parts.size > 2.if({ parts[2] }, { nil });

		// Handle octave part
		if(octavePart.notNil and: { octavePart.size == 1 and: octavePart[0].isDecDigit }) {
			octave = octavePart[0].digit;
			over = nil;
		} {
			// otherwise treat it as an inversion note
			over = NoteSymbol.asNote(octavePart);
		};

		// Handle duration
		if(durPart.notNil and: { durPart[0].isDecDigit }) {
			dur = NoteSymbol.asDuration(durPart);
		};

		// Try to match chord shape by shortening from left until matched
		shape = shapes[name.asSymbol];
		while({ shape.isNil and: { noteNameLength < 3 } }, {
			noteNameLength = noteNameLength + 1;
			shape = shapes[name.drop(noteNameLength).asSymbol];
		});

		// Extract root note from left portion
		if(noteNameLength > 0) {
			potentialRoot = name.keep(noteNameLength);
			root = NoteSymbol.asNote(potentialRoot);

			if(root == potentialRoot) { ^input };

			// Default to major if chord type not found
			shape = shape ?? { shapes.major };
		};

		// Apply octave shift to root
		root = root + (octave * 12);

		// If inversion specified, sort notes upward from inversion
		if(over.notNil and: shape.notNil) {
			octaveShift = 0;

			if(over < root) { octaveShift = 12 };

			shape = shape.collect { |note|
				if(note < (over - root + octaveShift)) {
					note + 12
				} {
					note
				}
			};

			shape = shape - octaveShift;
		};

		chord = (root + shape).sort;

		if(chord.isString) { ^input };

		dur !? { ^[chord, dur.asInteger] };

		^chord;
	}

	*asFreqs { |input|
		var notes = this.asNotes(input);

		if(notes.isArray) {
			^notes.collect(_.midicps);
		} {
			^notes.midicps;
		}
	}

    *new { |c| ^this.asNotes(c) }

}

NoteSymbol {
    classvar <notes;
    classvar <restNames;

    *initClass {
        // define note names
        notes = (c: 0, d: 2, e: 4, f: 5, g: 7, a: 9, b: 11);

        // bung in all the sharps and flats
        notes.keysValuesDo { |name val|
            notes[(name ++ \s).asSymbol] = val + 1;
            notes[(name ++ \b).asSymbol] = val - 1;
        };

        // set up rest names
        restNames = IdentitySet[\, \rest, \r, ""];
    }

    *asNote { |input|
        var octave = 0, note, dur, name;

        // reguritate anything we definately can't process
        if(input.isRest or: (input.isKindOf(String) or: input.isKindOf(Symbol)).not) {
            ^input
        };

        // make input a lowercase string
        name = input.asString.toLower;

        // if duration specified lop it off
        #name, dur = name.split($\_);

        // if name is a number we have a rest
        if(name.asInteger != 0) {
            ^[\, NoteSymbol.asDuration(name)];
        };

        // convert duration if specified
        dur = dur !? { NoteSymbol.asDuration(dur) };

        // if octave specified chop it off and shift note
        if(name.notNil and: { name.last.isDecDigit }) {
            octave = name.last.digit * 12 + 12;
            name = name.drop(-1);
        };

        // add the octave to the note number
        notes[name.asSymbol] !? { note = notes[name.asSymbol] + octave };

        // if duration was specified return that with note as tuple
        dur !? { note !? { ^[note, dur.asInteger] } };

        // if the note number was found return that
        note !? { ^note };

        // otherwise just throw out what came in
        ^input;
    }

    *asDegree { |name scale stepsPerOctave=12|
        ^NoteSymbol.asNote(name, scale, stepsPerOctave);
    }

	*asFreq { |input|
		var note = this.asNote(input);

		if(note.isArray) {
			^note[0].midicps  // assumes note with duration like [note, dur]
		} {
			^note.midicps
		}
	}

    *asDuration { |string|
        string = string.asString;
        if(string.size > 1) {
            // first digit is numerator second is denumerator
            ^string[0].digit / string[1].digit;
        } {
            ^string[0].digit;
        };
    }

    *noteName { |n| ^notes.findKeyForValue(n % 12) }

    *new { |c| ^this.asNotes(c) }
}

+ Symbol {
    // treat symbol as representing a chord
    asNotes { ^ChordSymbol.asNotes(this) }
    asDegrees { |scale notesPerOctave|
        ^ChordSymbol.asDegrees(this, scale, notesPerOctave)
    }

    // treat symbol as representing a note
    asNote { ^NoteSymbol.asNote(this) }
    asDegree { |scale notesPerOctave=12|
        ^NoteSymbol.asDegree(this, scale, notesPerOctave)
    }

	asFreqs { ^ChordSymbol.asFreqs(this) }
	asFreq { ^NoteSymbol.asFreq(this) }

    asNoteOrChord {
        var ns, cs;

        // return note number if it makes a note
        ns = NoteSymbol.asNote(this);
        if(ns != this) { ^ns };

        // return chord if it returns a chord
        cs = ChordSymbol.asNotes(this);
        if(cs != this) { ^cs };

        // otherwise just return this
        ^this;
    }

    // override what happens when Symbol is embedded in a stream
    embedInStream { ^this.asNoteOrChord.yield }

    // work out wether or not this is a rest or not
    isRest {
        ^this.isMap.not
        and: { ^NoteSymbol.restNames.findMatch(this).notNil }
        and: { ^NoteSymbol.asNote(this).asArray[0] }
        and: { ^ChordSymbol.asNotes(this)[0] == \ }
    }
}

+ SequenceableCollection {
    // try converting to, note then chord
    progression {
        ^this.collect { |s| s.asNoteOrChord }
    }

    // convienience for converting chord names to notes
    chordProg {
        ^ChordSymbol.noteProgression(this);
    }

    chordProgDegrees { |scale notesPerOctave=12|
        ^ChordSymbol.degreeProgression(this, scale, notesPerOctave);
    }

    noteProg {
        ^this.collect { |name| name.asNote };
    }

    noteProgDegree { |scale notesPerOctave=12|
        ^this.collect { |name| name.asDegree(scale, notesPerOctave) }
    }

	chordProgFreqs {
		^this.collect { |s| s.asFreqs };
	}

	noteProgFreq {
		^this.collect { |name| name.asFreq };
	}

    // converts a given key/note to a degree
    // TODO won't be required in next SC release see pull request #1164
    performKeyToDegree2 { |key stepsPerOctave=12|
        var nearestDegree, closestScale, sharpening, octave;

        stepsPerOctave = stepsPerOctave ?? 12;

        // store away octave and wrap key inside a single one
        octave = (key / stepsPerOctave).floor;
		key = key % stepsPerOctave;

        // find the closest degree in scale for all keys in octave
        closestScale = (0..stepsPerOctave).collect { |k|
            this.indexInBetween(k).floor
        };

        // find the closest degree in the scale for the key
		nearestDegree = this.indexInBetween(key).floor;

        // calculate how much to sharpen our degree by
        sharpening = closestScale.indexOf(nearestDegree) - key / -10;

        ^nearestDegree + sharpening + (octave * this.size);
    }

}

+ SimpleNumber {
    // TODO won't be required in next SC release see pull request #1164
    keyToDegree2 { |scale stepsPerOctave=12| // collection is presumed to be sorted
        scale = scale ?? { Scale.major.degrees };
        ^scale.performKeyToDegree2(this, stepsPerOctave)
    }
}

+ Scale {
    // TODO won't be required in next SC release see pull request #1164
    performKeyToDegree2 { |key stepsPerOctave=12|
        ^degrees.performKeyToDegree2(key, stepsPerOctave)
    }
}
