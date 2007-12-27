package org.apache.harmony.tools.appletviewer;

import java.applet.AudioClip;
import java.net.URL;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;

class ViewerAudioClip implements AudioClip {
	//private Clip clip;
	private Sequencer sequencer;
	
	public ViewerAudioClip(URL url) {
		try {
			sequencer = MidiSystem.getSequencer();
			sequencer.open();
			
			Sequence sequence = MidiSystem.getSequence(url);
			sequencer.setSequence(sequence);
			
			if (!(sequencer instanceof Synthesizer)) {
				Synthesizer synthesizer = MidiSystem.getSynthesizer();
				synthesizer.open();
				Receiver receiver = synthesizer.getReceiver();
				Transmitter transmitter = sequencer.getTransmitter();
				transmitter.setReceiver(receiver);
			}
		} catch (Exception e) {
			sequencer = null;
		}
//		try {
//			this.clip = AudioSystem.getClip();
//			clip.open(AudioSystem.getAudioInputStream(url));
//		} catch (Exception e) {
//			this.clip = null;
//		}		
	}

	public void loop() {
		if (sequencer != null)
			sequencer.start();
//		if (clip != null) 
//			clip.loop(Clip.LOOP_CONTINUOUSLY);
	}

	public void play() {
		if (sequencer != null)
			sequencer.start();
//		if (clip != null) 
//			clip.start();		
	}

	public void stop() {
		if (sequencer != null)
			sequencer.stop();
//		if (clip != null) 
//			clip.stop();
	}
}
