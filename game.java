import javax.swing.;
import java.awt.;
import java.awt.event.;
import java.awt.geom.;
import java.io.;
import java.util.;
import java.util.List;

//ARCADE RUSH - a self-contained Java 17 arcade speedrun game.
//Open this file in jGRASP and click Build/Compile, then Run.
//No external libraries or assets are required.

public class ArcadeRush extends JPanel implements ActionListener, KeyListener, MouseListener {
static final int W=960, H=540, FPS=60, N=10;
static void tone(int hz,int millis){
Thread sound=new Thread(()->{try{
float rate=22050;int samples=(int)(ratemillis/1000.0);byte[] data=new byte[samples];
for(int i=0;i<samples;i++){double envelope=1.0-i/(double)samples;data[i]=(byte)(Math.sin(2Math.PIhzi/rate)55envelope);}
javax.sound.sampled.AudioFormat format=new javax.sound.sampled.AudioFormat(rate,8,1,true,false);
javax.sound.sampled.SourceDataLine line=javax.sound.sampled.AudioSystem.getSourceDataLine(format);
line.open(format);line.start();line.write(data,0,data.length);line.drain();line.close();
}catch(Exception ignored){}});sound.setDaemon(true);sound.start();
}
static final Color BG=new Color(12,16,30), PANEL=new Color(24,32,54), CYAN=new Color(68,225,232),
GOLD=new Color(255,205,75), PINK=new Color(255,91,145), GREEN=new Color(105,235,142),
WHITE=new Color(238,243,255), MUTED=new Color(145,160,190);
enum State { MENU, PLAY, TRANSITION, PAUSE, RESULTS, PRACTICE, CONTROLS }
State state=State.MENU;
javax.swing.Timer loop;
MiniGame game;
int level=0, deaths=0, menuChoice=0, practiceChoice=0;
long runStart, pauseAt, transitionAt, levelStart, finishedAt, finalElapsed;
long[] levelTimes=new long[N];
long best=Long.MAX_VALUE;
int bestDeaths=Integer.MAX_VALUE;
boolean practice=false;
String banner="";
float bannerAlpha=0;
Random random=new Random(7);
Font title=new Font("Monospaced",Font.BOLD,42), heading=new Font("Monospaced",Font.BOLD,24),
body=new Font("Monospaced",Font.BOLD,15), small=new Font("Monospaced",Font.PLAIN,12);
public ArcadeRush(){
setPreferredSize(new Dimension(W,H)); setBackground(BG); setFocusable(true); setFocusTraversalKeysEnabled(false);
addKeyListener(this); addMouseListener(this);
loadBest();
loop=new javax.swing.Timer(1000/FPS,this); loop.start();
}
public static void main(String[] args){
SwingUtilities.invokeLater(()->{
JFrame f=new JFrame("ARCADE RUSH");
ArcadeRush p=new ArcadeRush(); f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
f.setContentPane(p); f.pack(); f.setMinimumSize(new Dimension(720,420));
f.setLocationRelativeTo(null); f.setVisible(true); p.requestFocusInWindow();
});
}
void loadBest(){ try(Scanner s=new Scanner(new File("arcade_rush_record.txt"))){best=s.nextLong();bestDeaths=s.nextInt();}catch(Exception ignored){} }
void saveBest(){try(PrintWriter p=new PrintWriter("arcade_rush_record.txt")){p.println(best);p.println(bestDeaths);}catch(Exception ignored){}}
String time(long ms){return String.format("%02d:%02d.%02d",ms/60000,(ms/1000)%60,(ms/10)%100);}
long elapsed(){
// Snapshot the run clock at the instant the final level is cleared.
// RESULTS always reads this frozen value, never the live system clock.
if(state==State.RESULTS || finishedAt>0) return Math.max(0,finalElapsed);
long end=(state==State.PAUSE)?pauseAt:System.currentTimeMillis();
return Math.max(0,end-runStart);
}
void startRun(boolean p){
practice=p; deaths=0; Arrays.fill(levelTimes,0); level=0; finishedAt=0; finalElapsed=0;
runStart=System.currentTimeMillis(); startLevel();
}
void startLevel(){
game=createGame(level); game.start(); levelStart=System.currentTimeMillis();
state=practice?State.PRACTICE:State.PLAY;
}
MiniGame createGame(int i){
switch(i){
case 0:return new Snake();
case 1:return new Rhythm();
case 2:return new Pac();
case 3:return new Frog();
case 4:return new Mines();
case 5:return new Breakout();
case 6:return new Flappy();
case 7:return new Invaders();
case 8:return new Platformer();
case 9:return new Sokoban();
default:return new Sokoban();
}
}
void died(){deaths++;tone(180,110);game.reset(); banner="TRY AGAIN"; bannerAlpha=1;}
void completed(){
tone(660,100);
if(practice){state=State.MENU;game=null;return;}
levelTimes[level]=System.currentTimeMillis()-levelStart;
if(level==N-1){finishedAt=System.currentTimeMillis();finalElapsed=Math.max(0,finishedAt-runStart);if(finalElapsed<best){best=finalElapsed;bestDeaths=deaths;saveBest();}state=State.RESULTS;game=null;}
else {state=State.TRANSITION;transitionAt=System.currentTimeMillis();banner="LEVEL CLEAR";bannerAlpha=1;}
}
@Override public void actionPerformed(ActionEvent e){
if(state==State.PLAY||state==State.PRACTICE){
game.update(1.0/FPS);
if(game.failed())died(); else if(game.complete())completed();
} else if(state==State.TRANSITION && System.currentTimeMillis()-transitionAt>850){level++;startLevel();}
if(bannerAlpha>0)bannerAlpha=Math.max(0,bannerAlpha-.025f);
repaint();
}
void drawCentered(Graphics2D g,String s,int y,Font f,Color c){
g.setFont(f);g.setColor(c);FontMetrics fm=g.getFontMetrics();g.drawString(s,(W-fm.stringWidth(s))/2,y);
}
void panel(Graphics2D g,int x,int y,int w,int h){
g.setPaint(new GradientPaint(x,y,new Color(35,48,78),x,y+h,new Color(17,24,43)));
g.fillRoundRect(x,y,w,h,20,20);
g.setColor(new Color(105,145,205,105));g.drawRoundRect(x,y,w,h,20,20);
g.setColor(new Color(255,255,255,18));g.drawLine(x+18,y+2,x+w-18,y+2);
}
@Override protected void paintComponent(Graphics gg){
super.paintComponent(gg); Graphics2D g=(Graphics2D)gg.create();
g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
double scale=Math.min(getWidth()/(double)W,getHeight()/(double)H);
int ox=(int)((getWidth()-Wscale)/2),oy=(int)((getHeight()-Hscale)/2);
g.setColor(Color.BLACK);g.fillRect(0,0,getWidth(),getHeight());
g.translate(ox,oy);g.scale(scale,scale);
if(state==State.MENU)drawMenu(g);
else if(state==State.CONTROLS)drawControls(g);
else if(state==State.RESULTS)drawResults(g);
else if(state==State.PAUSE){drawGame(g);g.setColor(new Color(0,0,0,175));g.fillRect(0,0,W,H);drawCentered(g,"PAUSED",245,title,GOLD);drawCentered(g,"P: RESUME ESC: MAIN MENU",285,body,WHITE);}
else {drawGame(g);if(state==State.TRANSITION){g.setColor(new Color(8,10,22,190));g.fillRect(0,0,W,H);drawCentered(g,banner,245,title,GOLD);drawCentered(g,"NEXT CHALLENGE",282,body,CYAN);}}
g.dispose();
}
void drawGame(Graphics2D g){
// Unified arcade cabinet presentation behind every minigame.
g.setPaint(new GradientPaint(0,0,new Color(7,12,27),W,H,new Color(17,27,49)));
g.fillRect(0,0,W,H);
g.setPaint(new RadialGradientPaint(new Point2D.Float(480,280),560,
new float[]{0f,.65f,1f},new Color[]{new Color(34,91,132,55),new Color(14,28,52,20),new Color(2,5,15,0)}));
g.fillRect(0,55,W,H-55);
// Fine grid and edge lights add depth without distracting from gameplay.
g.setStroke(new BasicStroke(1f));
g.setColor(new Color(75,155,205,15));
for(int x=18;x<W;x+=32)g.drawLine(x,62,x,H-10);
for(int y=70;y<H;y+=32)g.drawLine(0,y,W,y);
for(int i=0;i<42;i++){
int x=(i197+43)%W, y=65+(i113)%(H-75);
g.setColor(new Color(105,205,235,25+(i%3)9));
g.fillOval(x,y,2,2);
}
// Recessed playfield frame.
g.setColor(new Color(2,5,14,150));g.fillRoundRect(86,58,788,456,24,24);
g.setPaint(new GradientPaint(90,60,new Color(39,66,99,190),90,515,new Color(12,21,39,230)));
g.fillRoundRect(91,61,778,448,20,20);
g.setColor(new Color(96,177,220,95));g.setStroke(new BasicStroke(1.5f));
g.drawRoundRect(91,61,778,448,20,20);
g.setColor(new Color(255,255,255,25));g.drawLine(112,63,846,63);
game.render(g);
// Cabinet rails and corner indicators.
g.setStroke(new BasicStroke(3f));g.setColor(new Color(64,211,225,90));
g.drawLine(108,66,175,66);g.drawLine(785,504,852,504);
g.setStroke(new BasicStroke(1f));
g.setColor(new Color(5,9,20,245));g.fillRoundRect(0,0,W,56,0,0);
g.setPaint(new GradientPaint(0,0,new Color(20,34,57),0,55,new Color(7,13,27)));
g.fillRect(0,0,W,54);
g.setColor(new Color(75,195,220,115));g.drawLine(0,54,W,54);
// Stage marker.
g.setColor(new Color(40,63,91));g.fillRoundRect(18,8,114,38,10,10);
g.setColor(CYAN);g.setFont(new Font("Monospaced",Font.BOLD,12));
g.drawString(String.format("STAGE %02d / %02d",level+1,N),29,23);
g.setColor(WHITE);g.setFont(new Font("SansSerif",Font.BOLD,16));
g.drawString(gameNames()[level].toUpperCase(),150,32);
// Clock card, intentionally frozen on the results screen by elapsed().
g.setColor(new Color(35,48,70));g.fillRoundRect(650,7,158,39,10,10);
g.setColor(new Color(255,205,75,105));g.drawRoundRect(650,7,158,39,10,10);
g.setColor(GOLD);g.setFont(new Font("Monospaced",Font.BOLD,15));
g.drawString("TIME "+time(practice?System.currentTimeMillis()-levelStart:elapsed()),662,31);
g.setColor(new Color(53,35,62));g.fillRoundRect(818,7,78,39,10,10);
g.setColor(PINK);g.setFont(new Font("Monospaced",Font.BOLD,12));
g.drawString("FAIL "+deaths,830,31);
g.setColor(new Color(38,57,88));g.fillRoundRect(902,8,48,37,9,9);
g.setColor(new Color(82,200,225));g.drawRoundRect(902,8,48,37,9,9);
g.setFont(new Font("Monospaced",Font.BOLD,11));g.setColor(WHITE);g.drawString("TAB",913,31);
if(bannerAlpha>0){g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,bannerAlpha));drawCentered(g,banner,94,heading,GOLD);g.setComposite(AlphaComposite.SrcOver);}
g.setColor(new Color(160,180,210));g.setFont(small);
g.drawString("P PAUSE R RESTART ESC MENU TAB SKIP",22,530);
g.setColor(new Color(40,55,80));g.fillRoundRect(22,537,916,3,3,3);
g.setColor(CYAN);g.fillRoundRect(22,537,(int)(916.0(level+(practice?0:0))/N),3,3,3);
}
String[] gameNames(){return new String[]{"Rhythm Rush","Snake","Pac-Man","Frogger","Minesweeper","Breakout","Flappy Bird","Space Invaders","Platformer","Sokoban"};}
void drawMenu(Graphics2D g){
g.setPaint(new GradientPaint(0,0,new Color(5,10,25),W,H,new Color(29,19,55)));
g.fillRect(0,0,W,H);
g.setPaint(new RadialGradientPaint(new Point2D.Float(480,170),560,
new float[]{0f,.55f,1f},new Color[]{new Color(33,111,157,95),new Color(26,43,83,45),new Color(3,6,18,0)}));
g.fillRect(0,0,W,H);
// Starfield and orbital arcs.
for(int i=0;i<115;i++){
int x=(i173+31)%W,y=(i97+17)%H;
int z=1+(i%3);g.setColor(new Color(120,205,235,30+(i%5)12));g.fillOval(x,y,z,z);
}
g.setStroke(new BasicStroke(1.4f));g.setColor(new Color(80,164,210,35));
g.drawOval(625,34,430,430);g.drawOval(670,78,340,340);g.drawOval(710,118,260,260);
g.setColor(new Color(78,210,225,65));g.drawArc(670,78,340,340,25,95);
// Hero panel with layered shadow.
g.setColor(new Color(0,0,0,95));g.fillRoundRect(194,39,572,164,30,30);
g.setPaint(new GradientPaint(205,42,new Color(35,62,99,245),205,198,new Color(12,20,42,245)));
g.fillRoundRect(205,35,550,158,27,27);
g.setColor(new Color(100,208,235,135));g.setStroke(new BasicStroke(1.5f));g.drawRoundRect(205,35,550,158,27,27);
g.setColor(new Color(255,255,255,28));g.drawLine(229,38,730,38);
drawCentered(g,"ARCADE RUSH",108,new Font("SansSerif",Font.BOLD,48),CYAN);
drawCentered(g,"10 GAMES / ONE CONTINUOUS RUN",148,body,WHITE);
drawCentered(g,"A TEST OF SPEED, PRECISION & PATIENCE",174,small,MUTED);
String[] opts={"START RUN","PRACTICE MODE","CONTROLS","QUIT"};
for(int i=0;i<opts.length;i++){
int y=252+i49;boolean sel=i==menuChoice;
g.setColor(new Color(0,0,0,70));g.fillRoundRect(326,y-22,308,40,13,13);
g.setPaint(new GradientPaint(330,y-23,sel?new Color(52,105,133):new Color(31,44,68),330,y+17,sel?new Color(19,54,76):new Color(13,21,39)));
g.fillRoundRect(326,y-25,308,40,13,13);
g.setColor(sel?CYAN:new Color(75,105,145));g.drawRoundRect(326,y-25,308,40,13,13);
if(sel){g.setColor(CYAN);g.fillRoundRect(337,y-12,4,14,3,3);}
g.setFont(new Font("SansSerif",Font.BOLD,16));g.setColor(sel?GOLD:WHITE);
String label=opts[i];FontMetrics fm=g.getFontMetrics();g.drawString(label,480-fm.stringWidth(label)/2,y+1);
}
if(menuChoice==1){g.setFont(small);g.setColor(CYAN);drawCentered(g,"PRACTICE: "+gameNames()[practiceChoice].toUpperCase()+" (LEFT / RIGHT, SPACE)",454,small,CYAN);}
g.setColor(new Color(25,39,64,225));g.fillRoundRect(286,475,388,43,13,13);
g.setColor(new Color(87,171,203,100));g.drawRoundRect(286,475,388,43,13,13);
drawCentered(g,"PERSONAL BEST "+(best==Long.MAX_VALUE?"--:--.--":time(best))+" FAILS "+(bestDeaths==Integer.MAX_VALUE?"--":bestDeaths),502,small,GOLD);
drawCentered(g,"↑ / ↓ SELECT ENTER CONFIRM",535,small,MUTED);
}
void drawControls(Graphics2D g){
g.setPaint(new GradientPaint(0,0,new Color(6,12,28),W,H,new Color(23,24,54)));g.fillRect(0,0,W,H);
g.setPaint(new RadialGradientPaint(new Point2D.Float(480,230),500,new float[]{0f,1f},new Color[]{new Color(40,110,150,65),new Color(0,0,0,0)}));g.fillRect(0,0,W,H);
drawCentered(g,"HOW TO PLAY",91,title,CYAN);
g.setColor(new Color(0,0,0,90));g.fillRoundRect(180,126,600,340,22,22);
panel(g,185,121,590,330);
String[] lines={"ARROW KEYS Move, steer, or select","SPACE Jump, shoot, launch, or confirm","MOUSE Minesweeper tiles","P Pause / resume the speedrun","R Restart the current game (+1 fail)","TAB Skip the current game","ESC Return to the main menu"};
for(int i=0;i<lines.length;i++){
g.setColor(i==5?GOLD:WHITE);g.setFont(new Font("Monospaced",Font.BOLD,15));
g.drawString(lines[i],235,178+i36);
}
g.setColor(MUTED);g.setFont(body);drawCentered(g,"THE CLOCK RUNS THROUGH FAILURES AND LEVEL TRANSITIONS.",445,body,MUTED);
drawCentered(g,"PRESS ENTER OR ESC TO RETURN",510,small,GOLD);
}
void drawResults(Graphics2D g){
g.setPaint(new GradientPaint(0,0,new Color(5,11,27),W,H,new Color(27,20,55)));g.fillRect(0,0,W,H);
g.setPaint(new RadialGradientPaint(new Point2D.Float(480,110),500,new float[]{0f,.7f,1f},new Color[]{new Color(45,135,155,90),new Color(30,55,95,35),new Color(0,0,0,0)}));g.fillRect(0,0,W,H);
for(int i=0;i<80;i++){int x=(i173+31)%W,y=(i97+17)%H;g.setColor(new Color(120,210,235,25+(i%4)10));g.fillOval(x,y,2,2);}
drawCentered(g,"RUN COMPLETE",72,new Font("SansSerif",Font.BOLD,42),GOLD);
drawCentered(g,"ALL TEN CHALLENGES CLEARED",101,small,MUTED);
// Prominent final-time display: value is the frozen finalElapsed snapshot.
g.setColor(new Color(0,0,0,90));g.fillRoundRect(286,116,388,78,20,20);
g.setPaint(new GradientPaint(290,117,new Color(34,67,91),290,192,new Color(13,28,47)));
g.fillRoundRect(286,111,388,78,20,20);g.setColor(new Color(94,200,220,130));g.drawRoundRect(286,111,388,78,20,20);
drawCentered(g,"FINAL TIME",139,small,MUTED);
drawCentered(g,time(finalElapsed),176,new Font("Monospaced",Font.BOLD,30),CYAN);
drawCentered(g,"FAILURES "+deaths,216,body,WHITE);
g.setColor(new Color(0,0,0,75));g.fillRoundRect(125,235,710,223,18,18);
panel(g,130,230,700,220);
for(int i=0;i<N;i++){
int col=i%2,row=i/2,x=168+col345,y=265+row35;
g.setColor(new Color(45,66,95));g.fillRoundRect(x-9,y-20,322,28,7,7);
g.setFont(new Font("Monospaced",Font.BOLD,12));g.setColor(CYAN);g.drawString(String.format("%02d",i+1),x,y);
g.setColor(WHITE);g.drawString(gameNames()[i].toUpperCase(),x+35,y);
g.setColor(GOLD);g.drawString(time(levelTimes[i]),x+235,y);
}
drawCentered(g,"PERSONAL BEST "+time(best)+" BEST FAILS "+bestDeaths,483,body,GOLD);
drawCentered(g,"ENTER RETRY RUN M / ESC MENU",526,small,WHITE);
}
@Override public void keyPressed(KeyEvent e){
int k=e.getKeyCode();
if(state==State.MENU){
if(k==KeyEvent.VK_UP)menuChoice=(menuChoice+3)%4;
if(k==KeyEvent.VK_DOWN)menuChoice=(menuChoice+1)%4;
if(k==KeyEvent.VK_ENTER){if(menuChoice==0)startRun(false);if(menuChoice==1){practiceChoice=0;state=State.MENU;menuChoice=1;practice=true;}if(menuChoice==2)state=State.CONTROLS;if(menuChoice==3)System.exit(0);}
if(menuChoice==1&&k==KeyEvent.VK_LEFT)practiceChoice=(practiceChoice+9)%10;
if(menuChoice==1&&k==KeyEvent.VK_RIGHT)practiceChoice=(practiceChoice+1)%10;
if(menuChoice==1&&k==KeyEvent.VK_SPACE){practice=true;level=practiceChoice;runStart=System.currentTimeMillis();startLevel();}
}else if(state==State.CONTROLS){if(k==KeyEvent.VK_ESCAPE||k==KeyEvent.VK_ENTER)state=State.MENU;}
else if(state==State.RESULTS){if(k==KeyEvent.VK_ENTER)startRun(false);if(k==KeyEvent.VK_M||k==KeyEvent.VK_ESCAPE)state=State.MENU;}
else if(state==State.PAUSE){if(k==KeyEvent.VK_P){runStart+=System.currentTimeMillis()-pauseAt;state=State.PLAY;}if(k==KeyEvent.VK_ESCAPE){state=State.MENU;game=null;}}
else if(state==State.PLAY||state==State.PRACTICE){
if(k==KeyEvent.VK_P&&!practice){pauseAt=System.currentTimeMillis();state=State.PAUSE;}
else if(k==KeyEvent.VK_ESCAPE){state=State.MENU;game=null;}
else if(k==KeyEvent.VK_R)died();
else if(k==KeyEvent.VK_TAB)skipCurrentLevel();
else game.key(k,true);
}
repaint();
}
void skipCurrentLevel(){
if(game==null)return;
if(practice){game=null;state=State.MENU;}
else completed();
}
@Override public void keyReleased(KeyEvent e){if(game!=null)game.key(e.getKeyCode(),false);}
@Override public void mousePressed(MouseEvent e){
double s=Math.min(getWidth()/(double)W,getHeight()/(double)H);
int x=(int)((e.getX()-(getWidth()-Ws)/2)/s),y=(int)((e.getY()-(getHeight()-Hs)/2)/s);
if(game!=null&&(state==State.PLAY||state==State.PRACTICE)){
if(x>=810&&x<=945&&y>=7&&y<=45&&e.getButton()==MouseEvent.BUTTON1)skipCurrentLevel();
else game.mouse(x,y,e.getButton());
}
}
@Override public void keyTyped(KeyEvent e){} @Override public void mouseClicked(MouseEvent e){}
@Override public void mouseReleased(MouseEvent e){} @Override public void mouseEntered(MouseEvent e){} @Override public void mouseExited(MouseEvent e){}
abstract class MiniGame{
boolean done,dead; double t;
abstract void start(); abstract void render(Graphics2D g); abstract void key(int k,boolean down);
void update(double dt){t+=dt;} void mouse(int x,int y,int b){}
boolean complete(){return done;} boolean failed(){return dead;}
abstract void reset();
void text(Graphics2D g,String s,int x,int y,int size,Color c){g.setFont(new Font("Monospaced",Font.BOLD,size));g.setColor(c);g.drawString(s,x,y);}
void box(Graphics2D g,int x,int y,int w,int h,Color c){g.setColor(c);g.fillRoundRect(x,y,w,h,7,7);}
}
class Rhythm extends MiniGame{
// Slightly more forgiving pattern: fewer notes, wider spacing and a larger hit window.
int[] seq={0,1,2,3,0,2,1,3,3,2,0,1,2,2,3,0,1,3,2,0,1,2,3,1,0,2,3,0,1,2};
double[] notes=new double[seq.length];int hits=0,miss=0;boolean[] judged=new boolean[seq.length];
int[] colors={0xff54d9ff,0xff63e69a,0xffffd05a,0xffff70a8};
void start(){reset();for(int i=0;i<seq.length;i++)notes[i]=1.2+i.56;}
void reset(){done=dead=false;t=0;hits=miss=0;Arrays.fill(judged,false);}
void key(int k,boolean d){
if(!d)return;
int lane=k==37?0:k==40?1:k==38?2:k==39?3:-1;if(lane<0)return;
int bestI=-1;double diff=9;
for(int i=0;i<seq.length;i++)if(!judged[i]&&seq[i]==lane){double q=Math.abs((notes[i]+1.0)-t);if(q<diff){diff=q;bestI=i;}}
if(bestI>=0&&diff<.40){judged[bestI]=true;hits++;}else miss++;
}
void update(double dt){
super.update(dt);
for(int i=0;i<seq.length;i++)if(!judged[i]&&t>notes[i]+1.5){judged[i]=true;miss++;}
if(t>notes[seq.length-1]+1.7){if(hits100.0/seq.length>=85)done=true;else dead=true;}
}
void render(Graphics2D g){
g.setPaint(new GradientPaint(100,80,new Color(12,20,39),100,500,new Color(5,10,23)));
g.fillRoundRect(100,80,760,420,18,18);
for(int i=0;i<4;i++){
int x=270+i105;Color lane=new Color(colors[i],true);
g.setColor(new Color(0,0,0,70));g.fillRoundRect(x+3,103,90,390,12,12);
g.setPaint(new GradientPaint(x,100,new Color(lane.getRed(),lane.getGreen(),lane.getBlue(),90),x,490,new Color(15,25,43)));
g.fillRoundRect(x,100,90,390,12,12);
g.setColor(new Color(lane.getRed(),lane.getGreen(),lane.getBlue(),90));g.drawRoundRect(x,100,90,390,12,12);
g.setColor(new Color(255,255,255,20));g.drawLine(x+9,110,x+9,478);
text(g,new String[]{"←","↓","↑","→"}[i],x+30,480,27,WHITE);
}
g.setColor(new Color(255,205,75,45));g.fillRoundRect(255,379,435,17,8,8);
g.setColor(GOLD);g.fillRoundRect(255,385,435,5,3,3);
for(int i=0;i<seq.length;i++)if(!judged[i]){
int y=(int)(385-(notes[i]+1-t)165);
if(y>80&&y<490){int x=280+seq[i]105;Color c=new Color(colors[seq[i]],true);
g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),55));g.fillRoundRect(x-5,y-5,80,32,12,12);
g.setPaint(new GradientPaint(x,y,c,x,y+22,c.darker()));g.fillRoundRect(x,y,70,22,8,8);
g.setColor(new Color(255,255,255,100));g.drawLine(x+8,y+3,x+60,y+3);
}
}
text(g,"HITS "+hits+" MISSES "+miss+" HIT NEAR THE LINE",310,520,16,WHITE);
}
}
class Snake extends MiniGame{
// 8 x 6 keeps the arena compact. Five apples are active whenever five free cells exist.
int cols=8,rows=6,cell=48,sx=288,sy=145,dir=1,queuedDir=-1;
ArrayList<Point> body=new ArrayList<>();
ArrayList<Point> apples=new ArrayList<>();
double acc;
void start(){reset();}
void reset(){
done=dead=false;t=acc=0;dir=1;queuedDir=-1;body.clear();apples.clear();
body.add(new Point(2,3));body.add(new Point(1,3));body.add(new Point(0,3));
spawnApples();
}
void key(int k,boolean d){
if(!d)return;
int requested=k==38?0:k==39?1:k==40?2:k==37?3:-1;
if(requested<0)return;
int effective=queuedDir>=0?queuedDir:dir;
if(requested!=(effective+2)%4)queuedDir=requested;
}
void spawnApples(){
// Enumerate valid empty cells first; this guarantees the count decreases near the end
// and avoids an endless random-placement loop when fewer than five cells remain.
ArrayList<Point> empty=new ArrayList<>();
for(int y=0;y<rows;y++)for(int x=0;x<cols;x++){
Point candidate=new Point(x,y);
if(!body.contains(candidate)&&!apples.contains(candidate))empty.add(candidate);
}
Collections.shuffle(empty,random);
int needed=Math.min(5-apples.size(),empty.size());
for(int i=0;i<needed;i++)apples.add(empty.get(i));
}
void update(double dt){
super.update(dt);acc+=dt;if(acc<.14)return;acc=0;
if(queuedDir>=0){dir=queuedDir;queuedDir=-1;}
Point h=new Point(body.get(0));
if(dir==0)h.y--;if(dir==1)h.x++;if(dir==2)h.y++;if(dir==3)h.x--;
if(h.x<0||h.x>=cols||h.y<0||h.y>=rows||body.contains(h)){dead=true;return;}
body.add(0,h);
if(apples.contains(h)){
apples.remove(h);
if(body.size()==colsrows){done=true;return;}
spawnApples();
}else body.remove(body.size()-1);
}
void render(Graphics2D g){
g.setColor(new Color(4,10,22,170));g.fillRoundRect(sx-13,sy-13,colscell+20,rowscell+20,18,18);
text(g,"FILL THE BOARD "+body.size()+"/"+(colsrows)+" APPLES "+apples.size(),315,125,17,WHITE);
for(int y=0;y<rows;y++)for(int x=0;x<cols;x++){
int xx=sx+xcell,yy=sy+ycell;
g.setPaint(new GradientPaint(xx,yy,new Color(40,58,78),xx,yy+cell,new Color(19,31,49)));
g.fillRoundRect(xx,yy,cell-3,cell-3,9,9);
g.setColor(new Color(120,165,190,25));g.drawRoundRect(xx,yy,cell-3,cell-3,9,9);
}
for(Point a:apples){int ax=sx+a.xcell+11,ay=sy+a.ycell+11;
g.setColor(new Color(255,75,115,65));g.fillOval(ax-5,ay-5,35,35);
g.setPaint(new GradientPaint(ax,ay,new Color(255,145,165),ax+23,ay+25,new Color(205,38,82)));
g.fillOval(ax,ay,25,25);g.setColor(new Color(255,230,235));g.fillOval(ax+6,ay+5,7,7);
g.setColor(GREEN);g.drawLine(ax+13,ay+1,ax+16,ay-5);
}
for(int i=body.size()-1;i>=0;i--){Point p=body.get(i);int bx=sx+p.xcell+2,by=sy+p.ycell+2;
Color top=i==0?new Color(205,255,150):new Color(105,225,140);
Color bottom=i==0?new Color(75,190,100):new Color(30,125,85);
g.setPaint(new GradientPaint(bx,by,top,bx,by+cell,bottom));
g.fillRoundRect(bx,by,cell-7,cell-7,12,12);
g.setColor(new Color(255,255,255,55));g.drawLine(bx+7,by+5,bx+cell-15,by+5);
if(i==0){g.setColor(new Color(15,55,45));g.fillOval(bx+10,by+10,4,5);g.fillOval(bx+23,by+10,4,5);}
}
}
}
class Pac extends MiniGame{
final String[] original={
"###############",
"#.............#",
"#.###.###.###.#",
"#o#...#...#..o#",
"#.###.#.#.###.#",
"#.....#.#.....#",
"###.#.....#.###",
"#...#.###.#...#",
"#.#.........#.#",
"#o..#..#..#..o#",
"###############"
};
char[][] map=new char[11][15];
double px=1,py=1;
int dir=1,wantDir=1,lives=3,pellets=0,totalPellets=0;
double[] ghostX=new double[4],ghostY=new double[4];
int[] ghostDir=new int[4]; double powerTimer=0;
final int TILE=36,OX=210,OY=105;
final double PLAYER_SPEED=3.7,GHOST_SPEED=1.25;
void start(){reset();}
void reset(){
done=dead=false;t=0;px=1;py=1;dir=wantDir=1;lives=3;pellets=0;totalPellets=0;powerTimer=0;
int[][] starts={{13,1},{13,9},{7,5},{1,9}};
for(int i=0;i<4;i++){ghostX[i]=starts[i][0];ghostY[i]=starts[i][1];ghostDir[i]=3;}
for(int y=0;y<11;y++){map[y]=original[y].toCharArray();for(int x=0;x<15;x++)if(map[y][x]=='.'||map[y][x]=='o')totalPellets++;}
eatPellet(1,1);
}
boolean wall(int x,int y){return x<0||x>=15||y<0||y>=11||map[y][x]=='#';}
int dx(int d){return d==1?1:d==3?-1:0;}
int dy(int d){return d==2?1:d==0?-1:0;}
boolean atCenter(double v){return Math.abs(v-Math.rint(v))<0.00001;}
void key(int k,boolean down){
if(!down)return;
int requested=k==37?3:k==38?0:k==39?1:k==40?2:-1;
if(requested>=0)wantDir=requested;
}
void eatPellet(int x,int y){
if(x>=0&&x<15&&y>=0&&y<11&&(map[y][x]=='.'||map[y][x]=='o')){
boolean power=map[y][x]=='o';
map[y][x]=' ';pellets++;
if(power)powerTimer=7.0;
if(pellets>=totalPellets)done=true;
}
}
// Move from tile center to tile center in exact sub-steps. This avoids the
// rounding bug that previously reset the player to the same tile every frame.
void movePlayer(double distance){
int guard=0;
while(distance>0.000001&&!done&&guard++<8){
if(atCenter(px)&&atCenter(py)){
int tx=(int)Math.rint(px),ty=(int)Math.rint(py);
px=tx;py=ty;eatPellet(tx,ty);
if(!wall(tx+dx(wantDir),ty+dy(wantDir)))dir=wantDir;
if(dir<0||wall(tx+dx(dir),ty+dy(dir))){dir=-1;break;}
}
double toCenter;
if(dir==1)toCenter=Math.ceil(px+0.000001)-px;
else if(dir==3)toCenter=px-Math.floor(px-0.000001);
else if(dir==2)toCenter=Math.ceil(py+0.000001)-py;
else if(dir==0)toCenter=py-Math.floor(py-0.000001);
else break;
if(toCenter<0.000001)toCenter=1;
double step=Math.min(distance,toCenter);
px+=dx(dir)step;py+=dy(dir)step;distance-=step;
if(step>=toCenter-0.000001){px=Math.rint(px);py=Math.rint(py);eatPellet((int)px,(int)py);}
}
}
void moveGhosts(double dt){
for(int i=0;i<4;i++){
double remaining=GHOST_SPEED(powerTimer>0?0.78:1.0)dt;int guard=0;
while(remaining>0.000001&&guard++<5){
if(atCenter(ghostX[i])&&atCenter(ghostY[i])){
int gx=(int)Math.rint(ghostX[i]),gy=(int)Math.rint(ghostY[i]);
ghostX[i]=gx;ghostY[i]=gy;
ArrayList<Integer> choices=new ArrayList<>();
for(int d=0;d<4;d++)if(!wall(gx+dx(d),gy+dy(d)))choices.add(d);
if(choices.isEmpty()){ghostDir[i]=-1;break;}
// Avoid instant reversals unless the ghost is in a dead end.
if(choices.size()>1)choices.remove(Integer.valueOf((ghostDir[i]+2)%4));
int chosen=choices.get(random.nextInt(choices.size()));
if(powerTimer>0){
double best=-1;
for(int d:choices){double dist=Math.hypot(gx+dx(d)-px,gy+dy(d)-py);if(dist>best){best=dist;chosen=d;}}
}else if(random.nextDouble()<.35){
double best=Double.MAX_VALUE;
for(int d:choices){double dist=Math.hypot(gx+dx(d)-px,gy+dy(d)-py);if(dist<best){best=dist;chosen=d;}}
}
ghostDir[i]=chosen;
if(wall(gx+dx(chosen),gy+dy(chosen)))break;
}
int d=ghostDir[i];if(d<0)break;
double toCenter=(d==1?Math.ceil(ghostX[i]+0.000001)-ghostX[i]:d==3?ghostX[i]-Math.floor(ghostX[i]-0.000001):d==2?Math.ceil(ghostY[i]+0.000001)-ghostY[i]:ghostY[i]-Math.floor(ghostY[i]-0.000001));
if(toCenter<0.000001)toCenter=1;
double step=Math.min(remaining,toCenter);
ghostX[i]+=dx(d)step;ghostY[i]+=dy(d)step;remaining-=step;
if(step>=toCenter-0.000001){ghostX[i]=Math.rint(ghostX[i]);ghostY[i]=Math.rint(ghostY[i]);}
}
}
}
void update(double dt){
super.update(dt);
powerTimer=Math.max(0,powerTimer-dt);
movePlayer(PLAYER_SPEEDdt);
moveGhosts(dt);
for(int i=0;i<4;i++)if(Math.hypot(px-ghostX[i],py-ghostY[i])<.58){
if(powerTimer>0){
int[][] starts={{13,1},{13,9},{7,5},{1,9}};
ghostX[i]=starts[i][0];ghostY[i]=starts[i][1];ghostDir[i]=3;
}else{
lives--;if(lives<=0){dead=true;return;}
px=1;py=1;dir=wantDir=1;
int[][] starts={{13,1},{13,9},{7,5},{1,9}};
for(int j=0;j<4;j++){ghostX[j]=starts[j][0];ghostY[j]=starts[j][1];ghostDir[j]=3;}
break;
}
}
}
void render(Graphics2D g){
g.setPaint(new GradientPaint(190,75,new Color(7,14,34),190,505,new Color(3,7,20)));
g.fillRoundRect(190,75,580,430,18,18);
g.setColor(new Color(65,125,220,45));g.drawRoundRect(190,75,580,430,18,18);
for(int y=0;y<11;y++)for(int x=0;x<15;x++){
int xx=OX+xTILE,yy=OY+yTILE;
if(map[y][x]=='#'){
g.setColor(new Color(10,35,92));g.fillRoundRect(xx+1,yy+1,TILE-3,TILE-3,9,9);
g.setPaint(new GradientPaint(xx,yy,new Color(55,115,225),xx,yy+TILE,new Color(15,45,135)));
g.fillRoundRect(xx+4,yy+4,TILE-9,TILE-9,7,7);
g.setColor(new Color(135,185,255,100));g.drawRoundRect(xx+4,yy+4,TILE-9,TILE-9,7,7);
}else if(map[y][x]=='.'||map[y][x]=='o'){
int z=map[y][x]=='o'?12:5;
g.setColor(map[y][x]=='o'?new Color(255,205,75,70):new Color(240,245,255,45));
g.fillOval(xx+TILE/2-z,yy+TILE/2-z,z2,z2);
g.setColor(map[y][x]=='o'?GOLD:WHITE);
g.fillOval(xx+TILE/2-z/2,yy+TILE/2-z/2,z,z);
}
}
int ax=OX+(int)(pxTILE),ay=OY+(int)(pyTILE);
g.setColor(new Color(255,205,75,55));g.fillOval(ax-1,ay-1,38,38);
g.setPaint(new GradientPaint(ax,ay,new Color(255,245,155),ax+28,ay+30,new Color(235,145,25)));
int mouth=(int)(Math.abs(Math.sin(t10))35);
int start=dir==1?mouth:dir==3?180+mouth:dir==0?90+mouth:270+mouth;
g.fillArc(ax+4,ay+4,28,28,start,360-2mouth);
Color[] cs={PINK,CYAN,GREEN,new Color(180,120,255)};
for(int i=0;i<4;i++){
int gx=OX+(int)(ghostX[i]TILE),gy=OY+(int)(ghostY[i]TILE);
Color ghostColor=powerTimer>0?new Color(45,105,255):cs[i];
g.setColor(new Color(ghostColor.getRed(),ghostColor.getGreen(),ghostColor.getBlue(),55));g.fillOval(gx,gy,36,36);
g.setPaint(new GradientPaint(gx,gy,ghostColor.brighter(),gx,gy+31,ghostColor.darker()));
g.fillRoundRect(gx+5,gy+5,26,26,12,12);
g.setColor(WHITE);g.fillOval(gx+10,gy+11,6,7);g.fillOval(gx+20,gy+11,6,7);
g.setColor(new Color(25,35,65));g.fillOval(gx+12,gy+13,3,4);g.fillOval(gx+22,gy+13,3,4);
}
text(g,"PELLETS "+pellets+" / "+totalPellets+" LIVES "+lives+" HOLD ARROWS TO MOVE",245,520,15,WHITE);
if(powerTimer>0)text(g,"POWER "+String.format("%.1f",powerTimer)+"s",640,92,15,CYAN);
}
}
class Frog extends MiniGame{
final int COLS=9,ROWS=12,CELL=34,X0=327,Y0=75;
class Lane { double[] x; double speed,width; boolean river; Lane(double speed,double width,boolean river,double... starts){this.speed=speed;this.width=width;this.river=river;x=starts;} }
Lane[] lanes=new Lane[ROWS];double fx=4;int fy=11;
void start(){reset();}
void reset(){
done=dead=false;t=0;fx=4;fy=11;
lanes=new Lane[ROWS];
lanes[1]=new Lane(72,1.9,true,.0,3.0,6.1);
lanes[2]=new Lane(-88,1.6,true,1.0,4.1,7.2);
lanes[3]=new Lane(62,2.0,true,.2,3.4,6.6);
lanes[5]=new Lane(-112,1.35,false,.5,3.5,6.5);
lanes[6]=new Lane(145,1.15,false,1.8,5.2,8.0);
lanes[7]=new Lane(-98,1.55,false,0,4.5,7.5);
lanes[8]=new Lane(125,1.25,false,2.0,5.7);
}
void key(int k,boolean d){
if(!d)return;double nx=Math.rint(fx);int ny=fy;
if(k==37)nx--;if(k==39)nx++;if(k==38)ny--;if(k==40)ny++;
if(nx>=0&&nx<COLS&&ny>=0&&ny<ROWS){fx=nx;fy=ny;}
if(fy==0)done=true;
}
void update(double dt){
super.update(dt);
for(Lane lane:lanes)if(lane!=null){for(int i=0;i<lane.x.length;i++){lane.x[i]+=lane.speeddt/CELL;if(lane.speed>0&&lane.x[i]>COLS+1)lane.x[i]=-lane.width;if(lane.speed<0&&lane.x[i]<-lane.width)lane.x[i]=COLS+1;}}
if(fy>=1&&fy<=3){Lane lane=lanes[fy];boolean riding=false;for(double x:lane.x)if(fx+.78>=x&&fx+.22<=x+lane.width){riding=true;fx+=lane.speeddt/CELL;break;}if(!riding||fx<0||fx>COLS-1)dead=true;}
if(fy>=5&&fy<=8){Lane lane=lanes[fy];for(double x:lane.x)if(fx+.78>=x&&fx+.22<=x+lane.width){dead=true;break;}}
}
void render(Graphics2D g){
g.setColor(new Color(8,18,31));g.fillRoundRect(X0-12,Y0-8,COLSCELL+24,ROWSCELL+16,16,16);
for(int r=0;r<ROWS;r++){
int yy=Y0+rCELL;
Color c=(r==0||r==4||r>=9)?new Color(55,135,76):(r<=3?new Color(35,105,155):new Color(52,57,67));
g.setPaint(new GradientPaint(X0,yy,c,X0,yy+CELL,c.darker()));g.fillRect(X0,yy,COLSCELL,CELL-2);
if(r<=3){g.setColor(new Color(160,220,255,35));for(int q=0;q<3;q++){int wx=X0+((q103+(int)(t28))%(COLSCELL));g.drawArc(wx,yy+8,35,8,0,180);}}
if(r==0){for(int h=0;h<3;h++){int hx=X0+65+h145;g.setColor(new Color(25,95,45));g.fillOval(hx,yy+5,42,25);g.setColor(new Color(110,195,105));g.fillOval(hx+5,yy+5,30,18);}}
if(lanes[r]!=null){Lane lane=lanes[r];for(double x:lane.x){int xx=X0+(int)(xCELL);
if(lane.river){int lw=(int)(lane.widthCELL)-4;g.setColor(new Color(65,42,27));g.fillRoundRect(xx+2,yy+9,lw,25,11,11);g.setPaint(new GradientPaint(xx,yy+7,new Color(191,135,78),xx,yy+31,new Color(105,61,34)));g.fillRoundRect(xx,yy+7,lw,25,11,11);g.setColor(new Color(240,190,125,130));for(int k=0;k<3;k++)g.drawLine(xx+9+k13,yy+14,xx+17+k13,yy+14);}
else{
int cw=(int)(lane.widthCELL)-3;g.setColor(new Color(25,28,37));g.fillRoundRect(xx-2,yy+7,cw+4,27,9,9);
g.setPaint(new GradientPaint(xx,yy+6,new Color(245,90,85),xx,yy+30,new Color(135,35,50)));g.fillRoundRect(xx,yy+6,cw,24,7,7);
g.setColor(new Color(115,205,225));g.fillRoundRect(xx+cw/3,yy+9,Math.max(8,cw/3),9,4,4);
g.setColor(new Color(255,225,160));g.fillRoundRect(xx+4,yy+17,5,5,2,2);g.fillRoundRect(xx+cw-9,yy+17,5,5,2,2);
g.setColor(new Color(30,35,45));g.fillOval(xx+6,yy+25,10,8);g.fillOval(xx+cw-17,yy+25,10,8);
}
}}
}
int frogX=X0+(int)(fxCELL),frogY=Y0+fyCELL;
g.setColor(new Color(15,55,35,80));g.fillOval(frogX+3,frogY+20,30,9);
g.setPaint(new GradientPaint(frogX,frogY,new Color(190,255,135),frogX,frogY+29,new Color(55,160,75)));
g.fillOval(frogX+6,frogY+5,23,24);
g.setColor(new Color(40,100,45));g.fillOval(frogX+8,frogY+2,9,9);g.fillOval(frogX+20,frogY+2,9,9);
g.setColor(WHITE);g.fillOval(frogX+11,frogY+5,4,4);g.fillOval(frogX+23,frogY+5,4,4);
g.setColor(new Color(20,45,25));g.fillOval(frogX+12,frogY+6,2,2);g.fillOval(frogX+24,frogY+6,2,2);
text(g,"GET TO THE TOP. AVOID CARS; RIDE THE LOGS.",285,520,16,WHITE);
}
}
class Mines extends MiniGame{
int[][] board=new int[8][8];boolean[][] open=new boolean[8][8],flag=new boolean[8][8];int cursorX=0,cursorY=0;boolean seeded=false;
void start(){reset();}
void reset(){done=dead=false;t=0;seeded=false;for(int[]r:board)Arrays.fill(r,0);for(boolean[]r:open)Arrays.fill(r,false);for(boolean[]r:flag)Arrays.fill(r,false);cursorX=cursorY=0;}
void seed(int safeX,int safeY){int[][] mines={{0,7},{1,3},{1,6},{2,1},{2,5},{3,7},{4,2},{5,4},{6,0},{7,6}};for(int[]p:mines)if(Math.abs(p[0]-safeX)>1||Math.abs(p[1]-safeY)>1)board[p[1]][p[0]]=-1;for(int y=0;y<8;y++)for(int x=0;x<8;x++)if(board[y][x]!=-1){int n=0;for(int yy=-1;yy<=1;yy++)for(int xx=-1;xx<=1;xx++)if(y+yy>=0&&y+yy<8&&x+xx>=0&&x+xx<8&&board[y+yy][x+xx]==-1)n++;board[y][x]=n;}seeded=true;}
void reveal(int x,int y){if(x<0||x>=8||y<0||y>=8||open[y][x]||flag[y][x])return;if(!seeded)seed(x,y);if(board[y][x]==-1){dead=true;return;}open[y][x]=true;if(board[y][x]==0)for(int yy=-1;yy<=1;yy++)for(int xx=-1;xx<=1;xx++)reveal(x+xx,y+yy);int n=0;for(boolean[]r:open)for(boolean b:r)if(b)n++;if(n>=54)done=true;}
void key(int k,boolean d){if(!d)return;if(k==37)cursorX=Math.max(0,cursorX-1);if(k==39)cursorX=Math.min(7,cursorX+1);if(k==38)cursorY=Math.max(0,cursorY-1);if(k==40)cursorY=Math.min(7,cursorY+1);if(k==10||k==32)reveal(cursorX,cursorY);if(k==70)flag[cursorY][cursorX]=!flag[cursorY][cursorX];}
void mouse(int x,int y,int b){int gx=(x-320)/40,gy=(y-170)/40;if(gx>=0&&gx<8&&gy>=0&&gy<8){cursorX=gx;cursorY=gy;if(b==MouseEvent.BUTTON1)reveal(gx,gy);if(b==MouseEvent.BUTTON3)flag[gy][gx]=!flag[gy][gx];}}
void render(Graphics2D g){
text(g,"CLEAR THE FIELD | LEFT CLICK: REVEAL RIGHT CLICK: FLAG | F: FLAG",170,130,14,WHITE);
for(int y=0;y<8;y++)for(int x=0;x<8;x++){
int xx=320+x40,yy=170+y40;
if(open[y][x]){
g.setPaint(new GradientPaint(xx,yy,new Color(190,207,220),xx,yy+38,new Color(125,151,174)));
g.fillRoundRect(xx,yy,38,38,6,6);
if(board[y][x]>0){Color[] nums={WHITE,new Color(50,105,220),new Color(35,145,85),new Color(220,75,65),new Color(110,65,180)};g.setFont(new Font("SansSerif",Font.BOLD,19));g.setColor(nums[Math.min(board[y][x],4)]);g.drawString(""+board[y][x],xx+13,yy+26);}
}else{
g.setPaint(new GradientPaint(xx,yy,new Color(105,145,185),xx,yy+38,new Color(42,69,108)));
g.fillRoundRect(xx,yy,38,38,6,6);
g.setColor(new Color(210,230,250,75));g.drawLine(xx+5,yy+4,xx+32,yy+4);
if(flag[y][x]){g.setColor(GOLD);g.fillRect(xx+17,yy+8,3,20);g.fillPolygon(new int[]{xx+19,xx+31,xx+19},new int[]{yy+8,yy+13,yy+18},3);}
}
if(x==cursorX&&y==cursorY){g.setColor(WHITE);g.setStroke(new BasicStroke(2));g.drawRoundRect(xx+1,yy+1,35,35,6,6);g.setStroke(new BasicStroke(1));}
}
}
}
class Breakout extends MiniGame{
double x=480,y=400,vx=260,vy=-330,pad=480;boolean launched=false,left=false,right=false;
boolean[][] bricks=new boolean[5][9];int remaining=45,lives=3;
double paddleBoost=0,hazardClock=0;ArrayList<Rectangle> tnt=new ArrayList<>();
class Spark {double x,y,vx,vy,life;Color color;Spark(double x,double y,Color c){this.x=x;this.y=y;color=c;vx=(random.nextDouble()-.5)260;vy=(random.nextDouble()-.5)260;life=.45+random.nextDouble().35;}}
ArrayList<Spark> sparks=new ArrayList<>();
void start(){reset();}
void reset(){done=dead=false;t=0;x=480;y=425;vx=260;vy=-330;pad=480;launched=false;left=right=false;remaining=45;lives=3;paddleBoost=hazardClock=0;tnt.clear();sparks.clear();for(boolean[]r:bricks)Arrays.fill(r,true);}
void key(int k,boolean d){if(k==37)left=d;if(k==39)right=d;if(k==32&&d)launched=true;}
void update(double dt){
super.update(dt);paddleBoost=Math.max(0,paddleBoost-dt);
Iterator<Spark> sparkIt=sparks.iterator();
while(sparkIt.hasNext()){Spark p=sparkIt.next();p.x+=p.vxdt;p.y+=p.vydt;p.vy+=260dt;p.life-=dt;if(p.life<=0)sparkIt.remove();}
if(left)pad-=600dt;if(right)pad+=600dt;pad=Math.max(155,Math.min(805,pad));
hazardClock+=dt;
if(hazardClock>5.0){hazardClock=0;if(random.nextDouble()<.72)tnt.add(new Rectangle(150+random.nextInt(650),78,18,22));}
for(Rectangle bomb:tnt)bomb.y+=190dt;
Iterator<Rectangle> bombs=tnt.iterator();
while(bombs.hasNext()){Rectangle bomb=bombs.next();if(bomb.y>490){bombs.remove();continue;}
if(bomb.intersects(new Rectangle((int)pad-(paddleBoost>0?78:55),438,paddleBoost>0?156:110,28))){lives--;bombs.remove();if(lives<=0){dead=true;return;}}
}
if(!launched){x=pad;y=425;return;}
double oldY=y;x+=vxdt;y+=vydt;
if(x<110){x=110;vx=Math.abs(vx);}if(x>850){x=850;vx=-Math.abs(vx);}if(y<75){y=75;vy=Math.abs(vy);}
double paddleHalf=paddleBoost>0?78:55;
if(vy>0&&oldY<=442&&y>=442&&Math.abs(x-pad)<paddleHalf+8){y=442;vy=-Math.abs(vy);vx=Math.max(-520,Math.min(520,vx+(x-pad)2.6));}
if(y>500){lives--;if(lives<=0){dead=true;return;}launched=false;vx=190;vy=-250;return;}
for(int r=0;r<5;r++)for(int c=0;c<9;c++)if(bricks[r][c]){int bx=150+c74,by=105+r25;if(x+7>bx&&x-7<bx+64&&y+7>by&&y-7<by+18){bricks[r][c]=false;remaining--;
for(int q=0;q<14;q++)sparks.add(new Spark(bx+32,by+9,rowColorsFor(r)));
vy=-vy;double speed=Math.hypot(vx,vy);double newSpeed=Math.min(610,speed+13);double scale=newSpeed/Math.max(1,speed);vx=scale;vy=scale;
if(random.nextDouble()<.22)paddleBoost=6.0;
tone(420,28);if(remaining==0)done=true;return;}}
}
Color rowColorsFor(int r){return new Color[]{PINK,GOLD,GREEN,CYAN,new Color(170,125,255)}[r];}
void render(Graphics2D g){
g.setPaint(new GradientPaint(100,65,new Color(13,22,42),100,505,new Color(5,10,24)));
g.fillRoundRect(100,65,760,440,20,20);
Color[] rowColors={PINK,GOLD,GREEN,CYAN,new Color(170,125,255)};
for(int r=0;r<5;r++)for(int c=0;c<9;c++)if(bricks[r][c]){
int bx=150+c74,by=105+r25;Color col=rowColors[r];
g.setColor(new Color(col.getRed(),col.getGreen(),col.getBlue(),45));g.fillRoundRect(bx-2,by-2,68,22,8,8);
g.setPaint(new GradientPaint(bx,by,col.brighter(),bx,by+18,col.darker()));g.fillRoundRect(bx,by,64,18,6,6);
g.setColor(new Color(255,255,255,100));g.drawLine(bx+7,by+3,bx+56,by+3);
}
int paddleW=paddleBoost>0?156:110;
g.setColor(new Color(68,225,232,65));g.fillRoundRect((int)pad-paddleW/2-7,441,paddleW+14,22,12,12);
g.setPaint(new GradientPaint((int)pad-paddleW/2,445,CYAN,(int)pad-paddleW/2,459,new Color(25,120,160)));
g.fillRoundRect((int)pad-paddleW/2,445,paddleW,14,8,8);
g.setColor(new Color(255,255,255,130));g.drawLine((int)pad-paddleW/2+12,448,(int)pad+paddleW/2-12,448);
for(Spark p:sparks){g.setColor(new Color(p.color.getRed(),p.color.getGreen(),p.color.getBlue(),Math.max(0,Math.min(255,(int)(255p.life)))));g.fillOval((int)p.x,(int)p.y,4,4);}
for(Rectangle bomb:tnt){g.setColor(new Color(40,35,42));g.fillRoundRect(bomb.x,bomb.y,bomb.width,bomb.height,4,4);g.setColor(PINK);g.drawLine(bomb.x+4,bomb.y+5,bomb.x+14,bomb.y+15);g.setColor(GOLD);g.fillOval(bomb.x+12,bomb.y-4,6,6);}
if(paddleBoost>0)text(g,"WIDE PADDLE "+String.format("%.1f",paddleBoost)+"s",390,90,14,CYAN);
g.setColor(new Color(255,255,255,55));g.fillOval((int)x-13,(int)y-13,26,26);
g.setPaint(new RadialGradientPaint(new Point2D.Float((float)x-3,(float)y-3),12,new float[]{0f,1f},new Color[]{WHITE,CYAN}));g.fillOval((int)x-8,(int)y-8,16,16);
text(g,"BRICKS "+remaining+" BALLS "+lives+" DODGE TNT | WIDE PADDLE POWER-UP",210,520,14,WHITE);
}
}
class Flappy extends MiniGame{
class Pipe {double x,gapY;boolean scored=false;Pipe(double x,double gapY){this.x=x;this.gapY=gapY;}}
double birdY=270,velocity=0;final double birdX=245,gravity=900,flapVelocity=-340,pipeSpeed=205,gap=178;
int passed=0;ArrayList<Pipe> pipes=new ArrayList<>();boolean started=false;
void start(){reset();}
void reset(){done=dead=false;t=0;birdY=270;velocity=0;passed=0;started=false;pipes.clear();pipes.add(new Pipe(720,225));pipes.add(new Pipe(1040,315));pipes.add(new Pipe(1360,190));pipes.add(new Pipe(1680,285));pipes.add(new Pipe(2000,230));}
void key(int k,boolean d){if(d&&(k==32||k==38)){started=true;velocity=flapVelocity;}}
void update(double dt){
super.update(dt);if(!started)return;
velocity+=gravitydt;birdY+=velocitydt;
for(Pipe p:pipes){p.x-=pipeSpeeddt;if(!p.scored&&p.x+64<birdX){p.scored=true;passed++;if(passed>=5)done=true;}
boolean horizontal=birdX+13>p.x&&birdX-13<p.x+64;
if(horizontal&&(birdY-12<p.gapY-gap/2||birdY+12>p.gapY+gap/2))dead=true;
}
if(birdY<75||birdY>480)dead=true;
}
void render(Graphics2D g){
g.setPaint(new GradientPaint(100,65,new Color(65,151,205),100,485,new Color(175,220,235)));
g.fillRect(100,65,760,440);
// Distant soft cloud layers.
for(int i=0;i<6;i++){
int cx=(i193+80-(int)(t18)%193);
g.setColor(new Color(255,255,255,95));g.fillOval(cx,110+(i%3)65,70,25);g.fillOval(cx+20,99+(i%3)65,55,35);g.fillOval(cx+42,111+(i%3)65,55,22);
}
g.setColor(new Color(95,175,115));g.fillRect(100,485,760,20);
g.setColor(new Color(170,220,130));g.fillRect(100,485,760,5);
for(Pipe p:pipes){
int top=(int)(p.gapY-gap/2),bottom=(int)(p.gapY+gap/2),xx=(int)p.x;
g.setColor(new Color(10,80,45,55));g.fillRoundRect(xx+5,70,64,Math.max(0,top-65),8,8);
g.setPaint(new GradientPaint(xx,65,new Color(90,195,105),xx+64,65,new Color(20,105,60)));
g.fillRoundRect(xx,65,64,Math.max(0,top-65),8,8);
g.setPaint(new GradientPaint(xx,bottom,new Color(75,185,95),xx+64,bottom,new Color(15,95,55)));
g.fillRoundRect(xx,bottom,64,Math.max(0,485-bottom),8,8);
g.setPaint(new GradientPaint(xx-5,top-13,new Color(130,225,125),xx-5,top,new Color(35,135,70)));
g.fillRoundRect(xx-5,top-13,74,14,6,6);
g.setPaint(new GradientPaint(xx-5,bottom-1,new Color(130,225,125),xx-5,bottom+13,new Color(35,135,70)));
g.fillRoundRect(xx-5,bottom-1,74,14,6,6);
g.setColor(new Color(220,255,200,90));g.drawLine(xx+10,top-18,xx+10,75);g.drawLine(xx+10,bottom+18,xx+10,475);
}
int bx=(int)birdX,by=(int)birdY;
g.setColor(new Color(255,175,40,65));g.fillOval(bx-19,by-16,38,32);
g.setPaint(new GradientPaint(bx-12,by-12,new Color(255,240,125),bx+12,by+14,new Color(235,145,35)));
g.fillOval(bx-14,by-12,28,24);
g.setColor(new Color(215,120,35));g.fillOval(bx-7,by-1,15,10);
g.setColor(new Color(255,255,255,180));g.fillOval(bx+4,by-7,7,7);
g.setColor(new Color(35,45,55));g.fillOval(bx+7,by-5,3,4);
g.setColor(new Color(255,130,55));g.fillPolygon(new int[]{bx+10,bx+23,bx+10},new int[]{by+1,by+5,by+9},3);
text(g,"PIPES CLEARED "+passed+" / 5 SPACE OR UP TO FLAP",275,520,16,WHITE);
if(!started)text(g,"PRESS SPACE TO START",390,300,19,GOLD);
}
}
class Invaders extends MiniGame{
ArrayList<Rectangle> enemies=new ArrayList<>(),shots=new ArrayList<>(),bullets=new ArrayList<>();int px=470,lives=3;boolean left,right,fire;double fireCooldown=0;
void start(){reset();}void reset(){done=dead=false;t=0;px=470;lives=3;fireCooldown=0;fire=false;enemies.clear();shots.clear();bullets.clear();for(int r=0;r<2;r++)for(int c=0;c<8;c++)enemies.add(new Rectangle(180+c75,120+r55,32,24));}
void key(int k,boolean d){if(k==37)left=d;if(k==39)right=d;if(k==32)fire=d;}
void update(double dt){
super.update(dt);
if(left)px-=300dt;if(right)px+=300dt;px=Math.max(110,Math.min(820,px));
fireCooldown=Math.max(0,fireCooldown-dt);
if(fire&&fireCooldown<=0&&shots.size()<3){shots.add(new Rectangle(px+15,430,5,14));fireCooldown=.24;}
for(Rectangle shot:shots)shot.y-=480dt;
shots.removeIf(shot->shot.y<60);
for(Rectangle enemy:enemies){
enemy.x+=(Math.sin(t1.4)>0?1:-1);
if(random.nextDouble()<.003)bullets.add(new Rectangle(enemy.x+14,enemy.y+20,5,12));
}
for(Rectangle bullet:bullets)bullet.y+=240dt;
Iterator<Rectangle> si=shots.iterator();
while(si.hasNext()){
Rectangle shot=si.next();boolean hit=false;
Iterator<Rectangle> ei=enemies.iterator();
while(ei.hasNext())if(shot.intersects(ei.next())){ei.remove();hit=true;break;}
if(hit){si.remove();tone(520,35);}
}
Iterator<Rectangle> bi=bullets.iterator();
while(bi.hasNext()){
Rectangle bullet=bi.next();
if(bullet.y>480){bi.remove();continue;}
if(bullet.intersects(new Rectangle(px,450,40,20))){lives--;bi.remove();if(lives<=0)dead=true;}
}
if(enemies.isEmpty())done=true;
for(Rectangle enemy:enemies)if(enemy.y>420)dead=true;
}
void render(Graphics2D g){
g.setPaint(new GradientPaint(100,65,new Color(8,17,35),100,505,new Color(4,8,20)));
g.fillRoundRect(100,65,760,440,18,18);
for(int i=0;i<45;i++){int sx=120+(i137)%720,sy=82+(i83)%380;g.setColor(new Color(120,195,225,20+(i%4)10));g.fillOval(sx,sy,2,2);}
for(Rectangle e:enemies){
g.setColor(new Color(70,235,160,45));g.fillRoundRect(e.x-4,e.y-4,e.width+8,e.height+8,10,10);
g.setPaint(new GradientPaint(e.x,e.y,new Color(145,255,190),e.x,e.y+e.height,new Color(35,145,105)));
g.fillRoundRect(e.x,e.y,e.width,e.height,8,8);
g.setColor(new Color(15,55,45));g.fillOval(e.x+7,e.y+7,6,6);g.fillOval(e.x+20,e.y+7,6,6);
g.setColor(new Color(225,255,230));g.fillRect(e.x+5,e.y+3,5,2);g.fillRect(e.x+19,e.y+3,5,2);
g.setColor(new Color(30,95,70));g.drawLine(e.x+5,e.y+e.height-3,e.x+e.width-5,e.y+e.height-3);
}
int[] shipX={px+20,px+3,px+9,px+31,px+37},shipY={432,466,459,459,466};
g.setColor(new Color(68,225,232,55));g.fillOval(px-5,438,50,36);
g.setPaint(new GradientPaint(px,432,CYAN,px,466,new Color(20,85,145)));
g.fillPolygon(shipX,shipY,5);g.setColor(new Color(220,255,255));g.drawLine(px+20,435,px+20,458);
g.setColor(GOLD);for(Rectangle shot:shots){g.fillRoundRect(shot.x,shot.y,shot.width,shot.height,4,4);g.setColor(new Color(255,245,160,90));g.fillOval(shot.x-3,shot.y,11,8);g.setColor(GOLD);}
g.setColor(PINK);for(Rectangle bullet:bullets){g.fillRoundRect(bullet.x,bullet.y,bullet.width,bullet.height,4,4);g.setColor(new Color(255,110,160,75));g.fillOval(bullet.x-3,bullet.y-2,11,15);g.setColor(PINK);}
text(g,"ENEMIES "+enemies.size()+" LIVES "+lives,390,520,17,WHITE);
}
}
class Platformer extends MiniGame{
class Platform {int x,y,w;Platform(int x,int y,int w){this.x=x;this.y=y;this.w=w;}}
double x=110,y=396,vy=0,camera=0;boolean left=false,right=false,onGround=true;
ArrayList<Platform> platforms=new ArrayList<>();ArrayList<Rectangle> spikes=new ArrayList<>();final int pw=26,ph=34;final int worldWidth=2240;
double sawPhase=0;
void start(){reset();}
void reset(){
done=dead=false;t=0;x=110;y=396;vy=0;camera=0;left=right=false;onGround=true;platforms.clear();spikes.clear();sawPhase=0;
platforms.add(new Platform(80,430,95));
platforms.add(new Platform(220,385,48));
platforms.add(new Platform(330,330,42));
platforms.add(new Platform(445,375,46));
platforms.add(new Platform(565,300,42));
platforms.add(new Platform(685,350,44));
platforms.add(new Platform(805,270,42));
platforms.add(new Platform(925,330,44));
platforms.add(new Platform(1045,255,42));
platforms.add(new Platform(1165,315,44));
platforms.add(new Platform(1285,235,42));
platforms.add(new Platform(1405,300,44));
platforms.add(new Platform(1525,220,42));
platforms.add(new Platform(1645,300,48));
platforms.add(new Platform(1780,365,55));
platforms.add(new Platform(1920,300,48));
platforms.add(new Platform(2055,430,125));
spikes.add(new Rectangle(455,356,28,18));
spikes.add(new Rectangle(935,311,26,18));
spikes.add(new Rectangle(1415,281,25,18));
}
void key(int k,boolean d){
if(k==37)left=d;if(k==39)right=d;
if(d&&(k==32||k==38)&&onGround){vy=-535;onGround=false;}
}
void update(double dt){
super.update(dt);sawPhase+=dt;double oldBottom=y+ph;double horizontal=(right?285:0)-(left?285:0);x+=horizontaldt;x=Math.max(80,Math.min(worldWidth-pw,x));
vy+=1250dt;y+=vydt;onGround=false;
if(vy>=0){for(Platform p:platforms){double newBottom=y+ph;if(x+pw>p.x&&x<p.x+p.w&&oldBottom<=p.y+3&&newBottom>=p.y){y=p.y-ph;vy=0;onGround=true;break;}}}
camera=Math.max(0,Math.min(worldWidth-800,x-300));
if(y+ph>=485||y>550)dead=true;
for(Rectangle spike:spikes)if(new Rectangle((int)x,(int)y,pw,ph).intersects(spike))dead=true;
double sawX=610+Math.sin(sawPhase1.8)48, sawY=270+Math.sin(sawPhase2.3)22;
if(Math.hypot(x+pw/2-sawX,y+ph/2-sawY)<27)dead=true;
double sawX2=1510+Math.sin(sawPhase1.55+1)42, sawY2=175+Math.cos(sawPhase2)18;
if(Math.hypot(x+pw/2-sawX2,y+ph/2-sawY2)<25)dead=true;
if(x>2070&&onGround&&y+ph<=435)done=true;
}
void render(Graphics2D g){
g.setPaint(new GradientPaint(80,65,new Color(25,55,83),80,485,new Color(12,24,43)));
g.fillRect(80,65,800,440);
// Parallax lights in the distant background.
for(int i=0;i<8;i++){int bx=100+i115-(int)(camera.22)%115;g.setColor(new Color(85,170,205,24));g.fillOval(bx,120+(i%3)48,75,75);}
g.setColor(new Color(25,75,55));g.fillRect(80,485,800,20);
g.setPaint(new GradientPaint(80,485,new Color(100,240,115),80,505,new Color(25,125,75)));g.fillRect(80,485,800,20);
for(int i=0;i<24;i++){int ax=80+i36;g.setColor(new Color(180,255,140,90));g.drawLine(ax,487,ax+8,496+(i%3)2);}
text(g,"ACID",100,480,11,WHITE);
for(Platform p:platforms){int sx=(int)(p.x-camera);if(sx>-150&&sx<950){
g.setColor(new Color(0,0,0,70));g.fillRoundRect(sx+3,p.y+5,p.w,15,7,7);
g.setPaint(new GradientPaint(sx,p.y,new Color(175,205,230),sx,p.y+16,new Color(55,85,125)));
g.fillRoundRect(sx,p.y,p.w,15,7,7);g.setColor(new Color(225,245,255,130));g.drawLine(sx+7,p.y+3,sx+p.w-7,p.y+3);
}}
for(Rectangle spike:spikes){int sxp=spike.x-(int)camera;g.setColor(PINK);for(int q=0;q<3;q++)g.fillPolygon(new int[]{sxp+q10,sxp+q10+5,sxp+q10+10},new int[]{spike.y+18,spike.y,spike.y+18},3);}
double sawX=610+Math.sin(sawPhase1.8)48, sawY=270+Math.sin(sawPhase2.3)22;
double sawX2=1510+Math.sin(sawPhase1.55+1)42, sawY2=175+Math.cos(sawPhase2)18;
for(double[] saw:new double[][]{{sawX,sawY},{sawX2,sawY2}}){int hx=(int)(saw[0]-camera),hy=(int)saw[1];g.setColor(new Color(255,80,110,65));g.fillOval(hx-22,hy-22,44,44);g.setColor(new Color(205,215,230));g.fillOval(hx-15,hy-15,30,30);g.setColor(new Color(70,80,100));for(int q=0;q<8;q++){double a=qMath.PI/4;g.drawLine(hx+(int)(Math.cos(a)8),hy+(int)(Math.sin(a)8),hx+(int)(Math.cos(a)20),hy+(int)(Math.sin(a)20));}}
int goalX=2070-(int)camera;g.setColor(new Color(255,205,75,45));g.fillRoundRect(goalX-7,382,42,53,10,10);
g.setColor(GOLD);g.fillRoundRect(goalX,390,28,40,6,6);g.setColor(new Color(255,245,180));g.drawLine(goalX+7,396,goalX+7,424);
int sx=(int)(x-camera);g.setColor(new Color(255,91,145,65));g.fillRoundRect(sx-5,(int)y-5,pw+10,ph+10,11,11);
g.setPaint(new GradientPaint(sx,(int)y,PINK,sx,(int)y+ph,new Color(155,35,95)));
g.fillRoundRect(sx,(int)y,pw,ph,7,7);
g.setColor(WHITE);g.fillOval(sx+6,(int)y+8,5,5);g.fillOval(sx+16,(int)y+8,5,5);
g.setColor(new Color(45,25,55));g.fillOval(sx+7,(int)y+9,2,3);g.fillOval(sx+17,(int)y+9,2,3);
g.setColor(new Color(35,55,80));g.fillRoundRect(100,72,760,7,4,4);
g.setColor(CYAN);g.fillRoundRect(100,72,Math.max(0,(int)(760x/worldWidth)),7,4,4);
text(g,"PRECISION COURSE | NARROW LEDGES | AVOID SPIKES & MOVING SAWS",155,520,14,WHITE);
}
}
class Sokoban extends MiniGame{
// Four-box puzzle with narrow passages and interior walls.
final String[] layout={
"##########",
"#........#",
"#..#..T..#",
"#..#..B..#",
"#........#",
"#.B.#.B..#",
"#..T...T.#",
"#..#B....#",
"#P...T...#",
"##########"
};
boolean[][] walls=new boolean[10][10],goals=new boolean[10][10],boxes=new boolean[10][10];
int x=1,y=8,moves=0,boxCount=0;
void start(){reset();}
void reset(){
done=dead=false;t=0;moves=0;boxCount=0;x=1;y=8;
for(int r=0;r<10;r++)for(int c=0;c<10;c++){
char ch=layout[r].charAt(c);walls[r][c]=ch=='#';goals[r][c]=ch=='T';boxes[r][c]=ch=='B';if(ch=='B')boxCount++;
}
}
void key(int k,boolean d){
if(!d)return;int dx=0,dy=0;if(k==37)dx=-1;if(k==39)dx=1;if(k==38)dy=-1;if(k==40)dy=1;if(dx==0&&dy==0)return;
int nx=x+dx,ny=y+dy;if(nx<0||nx>=10||ny<0||ny>=10||walls[ny][nx])return;
if(boxes[ny][nx]){int bx=nx+dx,by=ny+dy;if(bx<0||bx>=10||by<0||by>=10||walls[by][bx]||boxes[by][bx])return;boxes[ny][nx]=false;boxes[by][bx]=true;}
x=nx;y=ny;moves++;
int onGoals=0;for(int r=0;r<10;r++)for(int c=0;c<10;c++)if(boxes[r][c]&&goals[r][c])onGoals++;
if(onGoals==boxCount)done=true;
}
void render(Graphics2D g){
int s=44,x0=260,y0=85;
text(g,"PUSH ALL FOUR BOXES ONTO THE TARGETS",305,72,15,WHITE);
for(int r=0;r<10;r++)for(int c=0;c<10;c++){
int xx=x0+cs,yy=y0+rs;
if(walls[r][c]){
g.setPaint(new GradientPaint(xx,yy,new Color(100,125,160),xx,yy+s,new Color(35,50,78)));
g.fillRoundRect(xx,yy,s-2,s-2,7,7);
g.setColor(new Color(210,230,255,35));g.drawLine(xx+5,yy+5,xx+s-8,yy+5);
}else{
g.setPaint(new GradientPaint(xx,yy,new Color(48,62,82),xx,yy+s,new Color(22,33,51)));
g.fillRoundRect(xx,yy,s-2,s-2,7,7);
g.setColor(new Color(120,155,185,25));g.drawRoundRect(xx,yy,s-2,s-2,7,7);
}
if(goals[r][c]){
g.setColor(new Color(255,205,75,45));g.fillOval(xx+7,yy+7,28,28);
g.setColor(GOLD);g.setStroke(new BasicStroke(3));g.drawOval(xx+12,yy+12,18,18);g.setStroke(new BasicStroke(1));
}
if(boxes[r][c]){
Color a=goals[r][c]?new Color(100,230,145):new Color(205,145,85);
g.setColor(new Color(0,0,0,70));g.fillRoundRect(xx+8,yy+9,s-14,s-14,7,7);
g.setPaint(new GradientPaint(xx+6,yy+6,a.brighter(),xx+6,yy+s-8,a.darker()));
g.fillRoundRect(xx+6,yy+6,s-14,s-14,7,7);
g.setColor(new Color(255,240,210,110));g.drawRoundRect(xx+10,yy+10,s-22,s-22,5,5);
}
}
int px=x0+xs,py=y0+ys;
g.setColor(new Color(68,225,232,65));g.fillOval(px+3,py+3,s-9,s-9);
g.setPaint(new GradientPaint(px+8,py+7,CYAN,px+8,py+s-8,new Color(20,105,150)));
g.fillRoundRect(px+8,py+7,s-18,s-17,9,9);
g.setColor(WHITE);g.fillOval(px+16,py+15,5,5);g.fillOval(px+26,py+15,5,5);
text(g,"ARROWS TO MOVE MOVES "+moves+" BOXES ON TARGETS: "+countOnGoals()+"/"+boxCount,235,535,14,WHITE);
}
int countOnGoals(){int n=0;for(int r=0;r<10;r++)for(int c=0;c<10;c++)if(boxes[r][c]&&goals[r][c])n++;return n;}
}
}