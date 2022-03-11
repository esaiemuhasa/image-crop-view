/**
 * 
 */
package com.emuhasa.crop;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

/**
 * @author Esaie MUHASA
 *
 */
public class Lancher extends JFrame {
	private static final long serialVersionUID = -5519293247593540967L;
	
	private JButton btnSave = new JButton("Crop");
	private JFileChooser chooser = new JFileChooser();
	private ImagePicker image = new ImagePicker("Photo de profil");

	public Lancher() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("Graphique");
		this.getContentPane().add(image, BorderLayout.CENTER);
		this.getContentPane().add(btnSave, BorderLayout.SOUTH);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setResizable(false);
		
		this.btnSave.addActionListener(event -> {
			if(image.getSelectedFileName() == null){
				JOptionPane.showMessageDialog(this, "Choisissez une photo sur votre PC", "Avertissemnt", JOptionPane.WARNING_MESSAGE);
				return;
			}
			
			int status = chooser.showSaveDialog(this);
			if(status == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				try {
					ImageIO.write(image.getImage(), image.getImageType(), file);
				} catch (IOException e) {
					System.out.println("> "+e.getMessage());
				}
			}
		});
	}
	
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(new NimbusLookAndFeel());
		} catch (UnsupportedLookAndFeelException ex) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e) {}
		}
		Lancher l = new Lancher();
		l.setVisible(true);
	}

}
