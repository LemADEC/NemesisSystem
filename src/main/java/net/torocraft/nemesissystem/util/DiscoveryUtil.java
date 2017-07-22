package net.torocraft.nemesissystem.util;

import net.minecraft.world.World;
import net.torocraft.nemesissystem.discovery.NemesisDiscovery;
import net.torocraft.nemesissystem.registry.Nemesis;
import net.torocraft.nemesissystem.registry.NemesisRegistryProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class DiscoveryUtil {

    private static Random rand = new Random();

    private static int chance = 5;
    private static int attempts_ceiling = 12;

    public static NemesisDiscovery buildRandomDiscovery(World world, List<NemesisDiscovery> playerDiscoveries) {
        List<Nemesis> nemeses = NemesisRegistryProvider.get(world).list();
        Nemesis nemesis = getRandomNemesis(nemeses);
        NemesisDiscovery discovered = getPlayerDiscoveryForNemesis(nemesis.getId(), playerDiscoveries);
        int attempts = 0;
        while(isFullyDiscovered(discovered, nemesis) && ++attempts < attempts_ceiling) {
            nemesis = getRandomNemesis(nemeses);
            discovered = getPlayerDiscoveryForNemesis(nemesis.getId(), playerDiscoveries);
        }
        if (attempts == attempts_ceiling) {;
            System.out.println("too many attempts bailing out");
            return null;
        }
        NemesisDiscovery discovery = new NemesisDiscovery(nemesis.getId());
        setRandomInformation(discovery, nemesis);
        return discovery;
    }

    private static NemesisDiscovery getPlayerDiscoveryForNemesis(UUID id, List<NemesisDiscovery> playerDiscoveries) {
        for (NemesisDiscovery discovery : playerDiscoveries) {
            if (discovery.getNemesisId().equals(id)) {
                return discovery;
            }
        }
        return null;
    }

    private static Nemesis getRandomNemesis(List<Nemesis> nemeses) {
        return nemeses.get(rand.nextInt(nemeses.size()));
    }

    private static boolean isFullyDiscovered(NemesisDiscovery discovery, Nemesis nemesis) {
        if (discovery == null) {
            return false;
        }
        return discovery.isName() && discovery.isLocation() && discovery.getWeaknesses().size() == nemesis.getWeaknesses().size() &&
                discovery.getTraits().size() == nemesis.getTraits().size();
    }

    public static void setRandomInformation(NemesisDiscovery discovery, Nemesis nemesis) {
        boolean hasAddedInfo = false;
        if (!discovery.isName()) {
            if (rand.nextInt(chance) == 0) {
                discovery.setName(true);
                hasAddedInfo = true;
            }
        }
        if (!discovery.isLocation()) {
            if (rand.nextInt(chance) == 0) {
                discovery.setLocation(true);
                hasAddedInfo = true;
            }
        }
        if (discovery.getTraits().size() < nemesis.getTraits().size()) {
            if (rand.nextInt(chance) == 0) {
                setRandomIndex(discovery.getTraits(), nemesis.getTraits());
                hasAddedInfo = true;
            }
        }
        if (discovery.getWeaknesses().size() < nemesis.getWeaknesses().size()) {
            if (rand.nextInt(chance) == 0) {
                setRandomIndex(discovery.getWeaknesses(), nemesis.getWeaknesses());
                hasAddedInfo = true;
            }
        }

        if (!hasAddedInfo) {
            setRandomInformation(discovery, nemesis);
        }
    }

    /*
    There is surely a better way to do this; I'm just not very smart - zeriley
     */
    private static void setRandomIndex(List<Integer> a, List<? extends Object> l) {
        int random = rand.nextInt(l.size());

        if (!a.contains(random)) {
            a.add(random);
            return;
        }
        setRandomIndex(a, l);
    }

    public static List<NemesisDiscovery> merge(List<NemesisDiscovery> existing, NemesisDiscovery newDiscovery) {
        List<NemesisDiscovery> merged = new ArrayList<>();
        for (NemesisDiscovery discovery : existing) {
            merged.add(merge(discovery, newDiscovery));
        }
        return merged;
    }

    public static NemesisDiscovery merge(NemesisDiscovery discovery1, NemesisDiscovery discovery2) {
        if (!discovery1.getNemesisId().equals(discovery2.getNemesisId())) {
            return discovery1;
        }
        discovery1.setName(discovery1.isName() || discovery2.isName());
        discovery1.setLocation(discovery1.isLocation() || discovery2.isLocation());
        discovery1.getTraits().addAll(discovery2.getTraits());
        discovery1.getStrengths().addAll(discovery2.getStrengths());
        discovery1.getWeaknesses().addAll(discovery2.getWeaknesses());

        return discovery1;
    }

}