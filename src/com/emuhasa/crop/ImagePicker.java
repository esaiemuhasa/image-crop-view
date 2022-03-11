/**
 * 
 */
package com.emuhasa.crop;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Panel;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileFilter;


/**
 * @author Esaie MUHASA
 *
 */
public class ImagePicker extends JPanel {
	private static final long serialVersionUID = -8188839974929150048L;
	private static final Dimension PREFERRED_SIZE = new Dimension(260, 300);
	private static final String [] EXT = {"png", "jpg", "jpeg"};
	
	private JLabel title = new JLabel("", JLabel.CENTER);
	
	private JSlider slider = new JSlider(JSlider.VERTICAL);
	private ImagePickerRender render = new ImagePickerRender();
	private JButton btnChoose = new JButton("Choisir");
	
	private JFileChooser fileChooser = new JFileChooser();
	private Frame mainFrame;

	/**
	 * 
	 */
	public ImagePicker() {
		super(new BorderLayout());
		this.init();
	}
	
	public ImagePicker(String label) {
		this();
		this.title.setText(label);
	}
	
	public void setMaintFrame (Frame frame) {
		this.mainFrame = frame;
	}
	
	private void init() {
		final JPanel center = new JPanel(new BorderLayout());
		final Box box = Box.createVerticalBox();
		this.setPreferredSize(PREFERRED_SIZE);
		this.setMaximumSize(PREFERRED_SIZE);
		this.setMinimumSize(PREFERRED_SIZE);
		this.setBorder(new LineBorder(Color.WHITE));
		
		slider.setMaximum(200);
		slider.setMinimum(20);
		
		box.add(slider);
		box.add(Box.createVerticalStrut(20));
		
		this.add(box, BorderLayout.EAST);
		center.add(render, BorderLayout.CENTER);
		center.setBorder(new EmptyBorder(0, 5, 0, 0));
		
		Panel bottom = new Panel();
		bottom.add(btnChoose);
		
		this.add(title, BorderLayout.NORTH);
		this.add(center, BorderLayout.CENTER);
		this.add(bottom, BorderLayout.SOUTH);
		
		slider.setEnabled(false);
		
		fileChooser.setDialogTitle("Choisir une photo");
		fileChooser.setFileFilter(new ImagePickerFilter());
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		this.btnChoose.addActionListener(event -> {
			int result = fileChooser.showDialog(mainFrame, "Ouvrir");
			
			if(result == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				render.setFileName(file.getAbsolutePath());
				slider.setEnabled(true);
			} else {
				render.setFileName(null);
				slider.setEnabled(false);
			}
		});
		
		this.render.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2) {
					btnChoose.doClick();
				}
			}
		});
		
		slider.addChangeListener(event -> {
			render.setRation(slider.getValue());
		});
	}
	
	/**
	 * Renvoie le chemain absolut vers l'image sur le HDD
	 * @return
	 */
	public String getSelectedFileName () {
		return render.getFileName();
	}
	
	/**
	 * Renvoie le type de l'image.
	 * typiquement une petite chaine de caractere comme png, jpg ou jpeg
	 * @return
	 */
	public String getImageType () {
		if(getSelectedFileName() == null)
			return null;
		for (String e : EXT)
			if(getSelectedFileName().toLowerCase().matches(".+\\."+e))
				return e;
		
		return null;
	}
	
	/**
	 * Renvoie l'image deja redimensionner
	 * @return
	 */
	public BufferedImage getImage () {
		return render.cropImage();
	}
	
	
	/**
	 * Modification du zoom max possible
	 * @param ration
	 */
	public void setMaxZoom (int ration) {
		this.slider.setMaximum(ration);
	}
	
	/**
	 * Modification du zoom min possible
	 * @param ration
	 */
	public void setMinZoom (int ration) {
		this.slider.setMinimum(ration);
	}
	
	/**
	 * modification du ration actuel 
	 * doit etre une valeur comptix entre zoomMin et zoomMax
	 * @param ration
	 */
	public void setRation (int ration) {
		this.slider.setValue(ration);
	}
	
	public int getRation () {
		return this.slider.getValue();
	}
	
	/**
	 * 
	 * @author Esaie MUHASA
	 *
	 */
	private static class ImagePickerRender extends JComponent {
		private static final long serialVersionUID = 7268721008490974405L;
		private static int IMAGE_MAX_WIDTH = 150;
		private static final String DEFAULT_FILE_NAME = "personne.png";
		private static BufferedImage defaultImage;
		
		private String fileName;
		private BufferedImage image;
		
		//coordonnee de l'outil pour crop l'image
		private int xRect = 0;
		private int yRect = 0;
		
		//dimension de l'image
		private int xImg = 0;
		private int yImg = 0;
		private int wImg;//largeur de l'image (apres alcul du ration)
		private int hImg;//hauteur de l'image (apres calcul du ration)
		
		private MouseAdapter listener = new MouseAdapter() {
			
			private Point start;
			
			public void mouseDragged(MouseEvent e) {
				if(!imageReady())
					return;
				
				Point mouse = e.getPoint();
				
				if(start == null) {
					start = mouse;
					return;
				}
				
				int distX = (int) (mouse.getX() - start.getX()), distY = (int) (mouse.getY() - start.getY());
				move (xImg+distX, yImg+distY);
				
				start = mouse;
			};
			
			public void mousePressed(MouseEvent e) {	
				if(!imageReady())
					return;
				
				start = e.getPoint();
				ImagePickerRender.this.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			};
			
			public void mouseReleased(MouseEvent e) {
				if(!imageReady())
					return;
				
				ImagePickerRender.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				start = null;
			};
			
			/**
			 * Deplacement du cadre de crop
			 * @param x
			 * @param y
			 */
			private void move (int x, int y) {
				xImg = x;
				yImg = y;
				ImagePickerRender.this.repaint();
			}
			
			/**
			 * verification si l'image est deja selectionner
			 * @return
			 */
			protected boolean imageReady () {
				return fileName != null && image != null;
			}
			
		};
		
		public ImagePickerRender() {
			super();
			this.addMouseListener(listener);
			this.addMouseMotionListener(listener);
		}
		
		@Override
		public void doLayout() {
			super.doLayout();
			if(image == null) {
				initCropLook();
			}
			
			if(defaultImage == null) {
				try {
					 defaultImage = ImageIO.read(new File(DEFAULT_FILE_NAME));
				} catch (IOException e) {
				}
			}
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
			
			
			if (fileName != null) {				
				g2.drawImage(image, xImg, yImg, wImg, hImg, null);
			} else {
				initCropLook();
				g2.drawImage(defaultImage, xRect, yRect, IMAGE_MAX_WIDTH, IMAGE_MAX_WIDTH, null);			
			}
			
			
			g2.setColor(new Color(0x88000000, true));
			
			g2.fillRect(0, 0, getWidth(), yRect);
			g2.fillRect(0, getHeight()-yRect, getWidth(), yRect);
			
			g2.fillRect(0, yRect, xRect, getHeight()-yRect*2);
			g2.fillRect((xRect+ IMAGE_MAX_WIDTH), yRect, xRect, getHeight() - yRect*2);
			
			g2.setStroke(new BasicStroke(2f));
			g2.drawRect(1, 1, getWidth()-2, getHeight()-2);

			g2.setColor(Color.ORANGE);
			g2.drawRect(xRect, yRect, IMAGE_MAX_WIDTH, IMAGE_MAX_WIDTH);
		}

		/**
		 * @return the fileName
		 */
		public String getFileName() {
			return fileName;
		}

		/**
		 * @param fileName the fileName to set
		 */
		public void setFileName(String fileName) {
			this.fileName = fileName;
			if(fileName != null) {
				try {
					image = ImageIO.read(new File(fileName));
					setRation(50);
				} catch (IOException e) {
					
				}
			} else {
				image = null;
			}
			
			initCropLook();
			this.repaint();
		}
		
		private void initCropLook () {			
			xRect = this.getWidth()/2 - IMAGE_MAX_WIDTH/2;
			yRect = this.getHeight()/2 - IMAGE_MAX_WIDTH/2;
		}
		
		/**
		 * Pour manipuler la ration d'affichage de l'image
		 * @param ration une valeur entre 1 et 100
		 */
		public void setRation (int ration) {
			if(ration<=0 || ration > 200)
				throw new RuntimeException("ration doit etre une valeur compris entre 0 et 100: => "+ration);
			
			BigDecimal bigW = new BigDecimal(image.getWidth() * (ration/100.0)).setScale(0, RoundingMode.HALF_UP),
					bigH = new BigDecimal(image.getHeight() * (ration/100.0)).setScale(0, RoundingMode.HALF_UP);
			
			wImg = bigW.intValue();
			hImg = bigH.intValue();
			
			this.repaint();
		}
		
		/**
		 * renvoie l'image cropper
		 * @return
		 */
		public BufferedImage cropImage () {
			if(image == null)
				return null;
			
			double diffX = Math.abs(xImg - xRect),
					diffY = Math.abs(yImg - yRect);
			
			Image resize = image.getScaledInstance(wImg, hImg, Image.SCALE_DEFAULT);
			BufferedImage buffer = new BufferedImage(wImg, hImg, BufferedImage.TYPE_INT_BGR);
			buffer.createGraphics().drawImage(resize, 0, 0, null);
			
			BigDecimal bigX = new BigDecimal(diffX).setScale(0, RoundingMode.HALF_UP),
					bigY = new BigDecimal(diffY).setScale(0, RoundingMode.HALF_UP), 
					bigSize = new BigDecimal(IMAGE_MAX_WIDTH).setScale(0, RoundingMode.HALF_UP);
			
			int x = bigX.intValue(), y = bigY.intValue(), size = bigSize.intValue();
			BufferedImage crop = buffer.getSubimage(x, y, size, size);
			return crop;
		}
	}
	
	/**
	 * @author Esaie MUHASA
	 * Filtrage lors de la selection d'une image
	 */
	private static class ImagePickerFilter extends FileFilter {

		@Override
		public boolean accept(File f) {
			if(f.isDirectory())
				return true;
			
			for (String e : EXT)
				if(f.getAbsoluteFile().getName().toLowerCase().matches(".+\\."+e))
					return true;
			
			return false;
		}

		@Override
		public String getDescription() {
			return "Selectionner une image: (.png, .jpg et .jpeg)";
		}
		
	}

}
