package riding.riding;
import net.milkbowl.vault.economy.Economy;
import net.minecraft.server.v1_12_R1.IChatBaseComponent;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerListHeaderFooter;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

import static org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES;

public final class Riding extends JavaPlugin implements Listener, CommandExecutor {
    HashMap<UUID, Inventory> inv = new HashMap<>();
    Short back = 15;
    String userpath;
    List<String> ridingname = new ArrayList<>();
    HashMap<String, Short> ridingdur = new HashMap<>();//플러그인에 라이딩 등록
    HashMap<String, Boolean> isplayerdb =new HashMap<>();//있는지 없는지
    HashMap<UUID, List<String>> playerridingentity = new HashMap<>(); //탈 몹의 헤드 이름
    HashMap<UUID,Boolean> isplayermounting = new HashMap<UUID,Boolean>();//타는중인지
    HashMap<UUID,ArmorStand> playerriding = new HashMap<UUID,ArmorStand>();//타고있는 엔티티 (파괴할때만 필요)
    HashMap<UUID,String> selectedriding = new HashMap<UUID,String>();//어떤 라이딩을 탈건지(gui에서 설정)
    ConsoleCommandSender consol = Bukkit.getConsoleSender();
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        reloadDB();
        for(Player player:Bukkit.getOnlinePlayers())
        {
            isplayermounting.put(player.getUniqueId(), false);
            inv.put(player.getUniqueId(),Bukkit.createInventory(null, 54, "riding"));
            try {
                reload(player);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void reloadDB() {
        ridingname.clear();
        ridingdur.clear();
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
        if (!Folder2.exists()) {
            try{
                Folder2.mkdir(); //폴더 생성합니다.
                System.out.println("[Riding]폴더가 생성되었습니다.");
            }
            catch(Exception e){
                e.getStackTrace();
            }
        }
        File Folder3 = new File(folder2path);
        File[] files = Folder3.listFiles();
        if(files!=null)
        {
            for(File user : files)
            {
                String player =user.getName().substring(0,user.getName().lastIndexOf("t")-4);
                isplayerdb.put(player, true);
            }
        }


        String Filepath = folderpath+"\\riding.yml";
        File cffile = new File(Filepath);
        if (!cffile.exists()) {	// 파일이 존재하지 않으면 생성
            try {
                if (cffile.createNewFile())
                    System.out.println("[riding.yml]파일 생성 성공");
                else
                    System.out.println("[riding.yml]파일 생성 실패");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File file = new File("plugins/Riding", "riding.yml");
        FileConfiguration cnf = YamlConfiguration.loadConfiguration(file);
        consol.sendMessage(ChatColor.YELLOW+"---------------라이딩 로드---------------");

        if(cnf.contains("riding")){
            //consol.sendMessage("File exist");
            cnf = YamlConfiguration.loadConfiguration(file);
            List<String> ridings = (List<String>) cnf.getList("riding");
            if(!(ridings.isEmpty()))
            {
                for(String riding: ridings)
                {
                    try
                    {
                        ridingdur.put(riding.split("/")[0],Short.valueOf(riding.split("/")[1]));
                        ridingname.add(riding.split("/")[0]);
                        consol.sendMessage(ChatColor.GREEN+riding.split("/")[0]+"이 로드됨");
                    }
                    catch (Exception e)
                    {
                        consol.sendMessage(ChatColor.RED+riding+"이 로드되지 않음");
                    }
                }
            }
            else
            {
                consol.sendMessage(ChatColor.YELLOW+"로드할 riding이 없음");
            }
        }else{
            //consol.sendMessage("File doesnt exist");
            String[] list = {"(라이딩이름)/(내구도)"};
            cnf.set("riding", list);
            try {
                cnf.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //consol.sendMessage("Created");
        }
        consol.sendMessage(ChatColor.YELLOW+"---------------라이딩 완료---------------");
    }
    @Override
    public void onDisable() {
        for(Player player:Bukkit.getOnlinePlayers())
        {
            down(player);
        }
        //

        for(Player player : Bukkit.getOnlinePlayers())
        {
            try
            {
                saveplayer(player);
            }
            catch (Exception exception)
            {

            }
        }

    }
    @EventHandler
    public void onquit(PlayerQuitEvent e) {
        e.getPlayer().closeInventory();
        down(e.getPlayer());
        try
        {
            saveplayer(e.getPlayer());
        }
        catch (Exception exception)
        {

        }

    }
    public void saveplayer(Player player) throws IOException {
        File file = new File(userpath+"\\"+ChatColor.stripColor(player.getDisplayName())+".txt");
        FileWriter fw = new FileWriter(file);
        BufferedWriter writer = new BufferedWriter(fw);

        for(String str:playerridingentity.get(player.getUniqueId()))
        {
            if(str.equalsIgnoreCase(selectedriding.get(player.getUniqueId())))
            {
                str = "*"+str;
            }
            writer.write(str);
        }



        writer.close();
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(command.getName().equalsIgnoreCase("라이딩"))
        {
            Player player = (Player)sender;
            openInventory(player);
        }
        if(command.getName().equalsIgnoreCase("라이딩로드"))
        {
            reloadDB();

        }
        return true;
    }

    public void tabfooterstring(Player player , String footer){
        CraftPlayer craftplayer = (CraftPlayer) player;
        PlayerConnection connection = craftplayer.getHandle().playerConnection;
        IChatBaseComponent headerJson = IChatBaseComponent.ChatSerializer.a("{\"text\":\"\"}");
        IChatBaseComponent footerJson = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + footer + "\"}");
        PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();
        try {
            Field headerField = packet.getClass().getDeclaredField("a");
            headerField.setAccessible(true);
            headerField.set(packet, headerJson);
            headerField.setAccessible(!headerField.isAccessible());

            Field footerField = packet.getClass().getDeclaredField("b");
            footerField.setAccessible(true);
            footerField.set(packet, footerJson);
            footerField.setAccessible(!footerField.isAccessible());
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        connection.sendPacket(packet);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {

        isplayermounting.put(e.getPlayer().getUniqueId(),false);
        if(!playerridingentity.containsKey(e.getPlayer().getUniqueId()))
        {
            playerridingentity.put(e.getPlayer().getUniqueId(), new ArrayList<String>());
        }
        Player player = e.getPlayer();
        String playername = ChatColor.stripColor(e.getPlayer().getDisplayName());
        inv.put(player.getUniqueId(),Bukkit.createInventory(null, 54, "riding"));
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
                Folder.createNewFile();
            }
            catch(Exception ex){
                ex.getStackTrace();
            }
        }

        new BukkitRunnable()
        {

            @Override
            public void run()
            {
                if(!player.isOnline())
                {
                    this.cancel();
                }

                Economy economy = VaultEconomy.getEconomy();
                int money = (int)economy.getBalance(player);

                player.sendMessage(String.valueOf(money));
                tabfooterstring(player, ChatColor.YELLOW+"돈 : "+String.valueOf(money));
            }

        }.runTaskTimer(this, 0L, 1L);

    }

    public void reload(Player player) throws IOException {
        down(player);
        player.closeInventory();
        BufferedReader userfile = new BufferedReader(
                new FileReader(userpath+"\\"+ChatColor.stripColor(player.getDisplayName())+".txt")
        );
        String str;
        playerridingentity.put(player.getUniqueId(),new ArrayList<String>());
        while ((str = userfile.readLine())!=null)
        {
            if(str.charAt(0)=='*')
            {
                selectedriding.put(player.getUniqueId(),str.substring(1));
                playerridingentity.get(player.getUniqueId()).add(str.substring(1));
            }
            else
            {
                playerridingentity.get(player.getUniqueId()).add(str);
            }

        }
        userfile.close();
    }
    @EventHandler
    public void onPlayerpassivedown(PlayerToggleSneakEvent e)
    {
        down(e.getPlayer());
    }
    @EventHandler
    public void onPlayeritemwasteEvent(PlayerDropItemEvent e) {
        e.setCancelled(true);
        Player player = e.getPlayer();
        if(isplayermounting.get(player.getUniqueId()))
        {

            down(player);

        }
        else if(!player.isSneaking())
        {
            
            if((!selectedriding.containsKey(player.getUniqueId()))||selectedriding.get(player.getUniqueId())==null) {
                player.sendMessage(ChatColor.YELLOW+"선택된 라이딩이 없습니다.");
                return;
            }
            isplayermounting.put(player.getUniqueId(),true);

            ArmorStand entity = (ArmorStand)Bukkit.getWorld("world").spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
            entity.setAI(false);
            entity.setInvulnerable(true);
            entity.setHelmet(getarmorstandhead(selectedriding.get(player.getUniqueId())));
            playerriding.put(player.getUniqueId(),entity);
            entity.setPassenger(player);


        }

    }
    public ItemStack getarmorstandhead(String ridingname) {
        ItemStack ridingheaditem = new ItemStack(Material.IRON_AXE);
        ridingheaditem.setDurability(ridingdur.get(ridingname));
        return ridingheaditem;
    }
    public void down(Player player) {
        if(isplayermounting.get(player.getUniqueId()))
        {
            isplayermounting.put(player.getUniqueId(),false);

            playerriding.get(player.getUniqueId()).remove();
            playerriding.put(player.getUniqueId(),null);
        }
    }

    public void initializeItems(UUID uuid) {
        inv.get(uuid).clear();
        int i = 0;
        for(String ride:ridingname)
        {
            if(playerridingentity.containsKey(uuid)&&playerridingentity.get(uuid).contains(ride))//플레이어가 가지고있음
            {
                if(selectedriding.containsKey(uuid)&&ride.equalsIgnoreCase(selectedriding.get(uuid)))
                {
                    inv.get(uuid).setItem(i,createUIitem(Material.IRON_AXE, ride, ridingdur.get(ride), ChatColor.GREEN+"[selected]"));
                }
                else
                {
                    inv.get(uuid).setItem(i,createUIitem(Material.IRON_AXE, ride, ridingdur.get(ride)));
                }

            }
            else//안가짐
            {
                inv.get(uuid).setItem(i,createUIitem(Material.IRON_AXE, ChatColor.MAGIC+"???", (short)100, ChatColor.DARK_GRAY+"해금되지 않음"));
            }
            i++;
        }
        for(;i<54;i++)
        {
            inv.get(uuid).setItem(i,createUIitem(Material.IRON_AXE," ", (short)64));
        }
        inv.get(uuid).setItem(i-1, createUIitem(Material.IRON_AXE," ", back));
    }
    protected ItemStack createUIitem(final Material material, final String name,final short damage, final String... lore) {
        final ItemStack item = new ItemStack(material, 1,damage);
        final ItemMeta meta = item.getItemMeta();
        meta.setUnbreakable(true);
        meta.addItemFlags(HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        // Set the name of the item
        meta.setDisplayName(name);

        meta.setLore(Arrays.asList(lore));

        item.setItemMeta(meta);

        return item;
    }
    public void openInventory(final HumanEntity ent) {
        initializeItems(ent.getUniqueId());
        ent.openInventory(inv.get(ent.getUniqueId()));
    }
    @EventHandler
    public void onRightclick(PlayerInteractEvent e) {
        try
        {
            if(e.getItem().getType().equals(Material.IRON_AXE)&&e.getItem().getItemMeta().getDisplayName().startsWith("[탈것]"))
            {

            }
            else
            {
                return;
            }
        }
        catch (Exception ex)
        {
            return;
        }
        if(e.getAction()== Action.RIGHT_CLICK_AIR||e.getAction()==Action.RIGHT_CLICK_BLOCK)
        {
            playerridingentity.get(e.getPlayer().getUniqueId()).add(e.getItem().getItemMeta().getDisplayName().substring(4));

            e.getPlayer().getInventory().setItemInMainHand(null);
            e.getPlayer().sendMessage(ChatColor.GREEN+"등록되었습니다.");
        }
    }
    @EventHandler
    public void onItemclick(InventoryClickEvent e) {
        try{
            if(e.getClickedInventory().getItem(53).getType().equals(Material.IRON_AXE)&&e.getClickedInventory().getItem(53).getDurability()==back)
            {

            }
            else
            {
                return;
            }
        }
        catch (Exception ex)
        {
            return;
        }
        Player player = (Player)e.getWhoClicked();
        e.setCancelled(true);
        if(playerridingentity.get(player.getUniqueId()).contains(e.getCurrentItem().getItemMeta().getDisplayName()))
        {
            try
            {
                if(ChatColor.stripColor(e.getCurrentItem().getItemMeta().getLore().get(0)).equals("[selected]"))
                {
                    selectedriding.put(player.getUniqueId(),null);
                    initializeItems(player.getUniqueId());
                }
                else
                {
                    selectedriding.put(player.getUniqueId(),e.getCurrentItem().getItemMeta().getDisplayName());
                    initializeItems(player.getUniqueId());
                }
            }
            catch (Exception ex)
            {
                selectedriding.put(player.getUniqueId(),e.getCurrentItem().getItemMeta().getDisplayName());
                initializeItems(player.getUniqueId());
            }
        }
    }
}
