package riding.riding;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class Riding extends JavaPlugin implements Listener, CommandExecutor {
    String userpath;
    HashMap<String, Boolean> isplayerdb =new HashMap<>();//있는지 없는지
    HashMap<UUID, List<String>> playerridingentity = new HashMap<>(); //탈 몹의 헤드 이름
    HashMap<UUID,Boolean> isplayermounting = new HashMap<UUID,Boolean>();//타는중인지
    HashMap<UUID,Entity> playerriding = new HashMap<UUID,Entity>();//타고있는 엔티티
    ConsoleCommandSender consol = Bukkit.getConsoleSender();
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        for(Player player:Bukkit.getOnlinePlayers())
        {
            isplayermounting.put(player.getUniqueId(), false);

        }
        reloadDB();

    }
    public void reloadDB()
    {
        playerridingentity.clear();
        File pluginfile = new File("plugins","Riding.jar");
        consol.sendMessage(pluginfile.getAbsolutePath().split("Riding.jar")[0]+"Riding");
        String folderpath = pluginfile.getAbsolutePath().split("Riding.jar")[0]+"Riding";
        File Folder = new File(folderpath);
        if (!Folder.exists()) {
            try{
                Folder.mkdir(); //폴더 생성합니다.
                System.out.println("[Riding]폴더가 생성되었습니다.");
            }
            catch(Exception e){
                e.getStackTrace();
            }
        }
        String folder2path = pluginfile.getAbsolutePath().split("Riding.jar")[0]+"Riding\\Users";
        userpath = folder2path;
        File Folder2 = new File(folder2path);
        if (!Folder.exists()) {
            try{
                Folder.mkdir(); //폴더 생성합니다.
                System.out.println("[Riding]폴더가 생성되었습니다.");
            }
            catch(Exception e){
                e.getStackTrace();
            }
        }
        File[] files = Folder2.listFiles();
        for(File user : files)
        {
            String player =user.getName().substring(0,user.getName().lastIndexOf("t")-4);
            isplayerdb.put(player, true);
        }
        /*
        File Filepath = folderpath+"\\guisetting.yml";
        File cffile = new File(Filepath);
        if (!cffile.exists()) {	// 파일이 존재하지 않으면 생성
            try {
                if (cffile.createNewFile())
                    System.out.println("[guisetting.yml]파일 생성 성공");
                else
                    System.out.println("[guisetting.yml]파일 생성 실패");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File file = new File("plugins/PlayerManager", "guisetting.yml");
        FileConfiguration cnf = YamlConfiguration.loadConfiguration(file);
        consol.sendMessage(ChatColor.YELLOW+"---------------로드---------------");
        if(cnf.contains("gui")&&cnf.contains("command")){
            //consol.sendMessage("File exist");
            cnf = YamlConfiguration.loadConfiguration(file);
            List<String> guis = (List<String>) cnf.getList("gui");
            if(!(guis.isEmpty()))
            {
                for(String gui: guis)
                {
                    try
                    {
                        if(Integer.valueOf(gui.split("/")[1])==1)//playerhead
                        {
                            playerhead = Integer.valueOf(gui.split("/")[0]);
                        }
                        if(Integer.valueOf(gui.split("/")[1])==2)//playerjob
                        {
                            playerjob = Integer.valueOf(gui.split("/")[0]);
                            consol.sendMessage(ChatColor.YELLOW+"플레이어 직업은 보류");
                        }
                        if(Integer.valueOf(gui.split("/")[1])==3)//playerlevel
                        {
                            playerlevel = Integer.valueOf(gui.split("/")[0]);
                        }
                    }
                    catch (Exception e)
                    {
                        consol.sendMessage(ChatColor.RED+gui+"이 로드되지 않음");
                    }
                }
            }
            else
            {
                consol.sendMessage(ChatColor.YELLOW+"로드할 gui가 없음");
            }
            List<String> command = (List<String>) cnf.getList("command");
            if(!(command.isEmpty()))
            {
                for(String cmdd: command)
                {
                    try
                    {
                        cmd.put(Integer.valueOf(cmdd.split("/")[0]), cmdd.split("/")[1]);
                        ItemStack item = createGuidmaItem(Material.valueOf(cmdd.split("/")[2]),ChatColor.GRAY+cmdd.split("/")[3],Short.valueOf(cmdd.split("/")[4]), " ");
                        cmditem.put(Integer.valueOf(cmdd.split("/")[0]), item);
                    }
                    catch (Exception e)
                    {
                        consol.sendMessage(ChatColor.RED+cmdd+"이 로드되지 않음");
                    }

                }
            }
            else
            {
                consol.sendMessage(ChatColor.YELLOW+"로드할 command가 없음");
            }
        }else{
            //consol.sendMessage("File doesnt exist");
            String[] list = {"(몇번째칸인지)/(어떤건지1=플레이어 헤드,2=직업,3=마크렙)"};
            cnf.set("gui", list);
            String[] lists = {"(몇번째칸인지)/(커맨드)/(메테리얼ex)DIAMOND_SWORD)/(이름)/(내구도)"};
            cnf.set("command",lists);
            try {
                cnf.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //consol.sendMessage("Created");
        }*/

    }
    @Override
    public void onDisable() {
        for(Player player:Bukkit.getOnlinePlayers())
        {
            down(player);

        }
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args)
    {
        if(command.getName().equalsIgnoreCase("라이딩리로드"))
        {
            reloadDB();
            for(Player player:Bukkit.getOnlinePlayers())
            {
                try {
                    reload(player);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    @EventHandler
    public void onquit(PlayerQuitEvent e)
    {
        down(e.getPlayer());
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent e)
    {
        isplayermounting.put(e.getPlayer().getUniqueId(),false);
        Player player = e.getPlayer();
        String playername = ChatColor.stripColor(e.getPlayer().getDisplayName());
        if(isplayerdb.containsKey(playername)&&isplayerdb.get(playername))
        {
            try {
                reload(player);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        else
        {
            isplayerdb.put(playername,true);
            File Folder = new File(userpath+"\\"+playername+".txt");
            try{
                Folder.mkdir(); //폴더 생성합니다.
            }
            catch(Exception ex){
                ex.getStackTrace();
            }
        }

    }
    public void reload(Player player) throws IOException {
        down(player);
        player.closeInventory();
        BufferedReader userfile = new BufferedReader(
                new FileReader(userpath+"\\"+ChatColor.stripColor(player.getDisplayName())+".txt")
        );
        String str;
        playerridingentity.get(player.getUniqueId()).clear();
        while ((str = userfile.readLine())!=null)
        {
            playerridingentity.get(player.getUniqueId()).add(str);
        }
    }
    @EventHandler
    public void onPlayerpassivedown(PlayerToggleSneakEvent e)
    {
        down(e.getPlayer());
    }
    @EventHandler
    public void onArmorstanddamaged(EntityDamageEvent e)
    {
        if(e.getEntity().getType()==EntityType.ARMOR_STAND)
        {
            e.setCancelled(true);
        }
    }
    @EventHandler
    public void onPlayeritemwasteEvent(PlayerDropItemEvent e)
    {
        e.setCancelled(true);
        Player player = e.getPlayer();
        if(isplayermounting.get(player.getUniqueId()))
        {

            down(player);

        }
        else if(!player.isSneaking())
        {
            isplayermounting.put(player.getUniqueId(),true);

            Entity entity = Bukkit.getWorld("world").spawnEntity(player.getLocation(), EntityType.HORSE);
            
            playerriding.put(player.getUniqueId(),entity);
            entity.setPassenger(player);

        }

    }
    public void down(Player player)
    {
        if(isplayermounting.get(player.getUniqueId()))
        {
            isplayermounting.put(player.getUniqueId(),false);

            playerriding.get(player.getUniqueId()).remove();
        }
    }
}
