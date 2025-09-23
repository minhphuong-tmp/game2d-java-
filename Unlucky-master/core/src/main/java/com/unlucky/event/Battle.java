package com.unlucky.event;

import com.badlogic.gdx.math.MathUtils;
import com.unlucky.battle.Move;
import com.unlucky.battle.StatusEffect;
import com.unlucky.entity.enemy.Boss;
import com.unlucky.entity.enemy.Enemy;
import com.unlucky.entity.Player;
import com.unlucky.inventory.Item;
import com.unlucky.map.TileMap;
import com.unlucky.resource.ResourceManager;
import com.unlucky.resource.Util;
import com.unlucky.screen.GameScreen;

/**
 * Strings together battle events and manages calculations
 *
 * @author Ming Li
 */
public class Battle {

    // the enemy the player is facing
    public Enemy opponent;

    private GameScreen gameScreen;
    public TileMap tileMap;
    private Player player;

    // dmg reduction from heals, -1 if no reduction
    public int playerRed = -1;
    public int enemyRed = -1;
    // special move buffs
    public boolean[] buffs;

    // sacrifice percentage dmg
    public float psacrifice = 0;

    // cumulative damage by player over the battle
    public int cumulativeDamage = 0;
    // cumulative healing by player over the battle
    public int cumulativeHealing = 0;

    public Battle(GameScreen gameScreen, TileMap tileMap, Player player) {
        this.gameScreen = gameScreen;
        this.tileMap = tileMap;
        this.player = player;

        buffs = new boolean[Util.NUM_SPECIAL_MOVES];
        resetBuffs();
    }

    public void resetBuffs() {
        for (int i = 0; i < buffs.length; i++) buffs[i] = false;
    }

    /**
     * Sets and scales the enemy's stats according to its level
     * If the enemy is an elite, then its stats are between 1.3-1.6x higher
     * If boss, then stats are 2.4-3.0x higher
     *
     * @param opponent
     */
    public void begin(Enemy opponent) {
        this.opponent = opponent;

        // set opponent's level to be -1 to 1 added to the avg map level
        opponent.setLevel(Util.getDeviatedRandomValue(gameScreen.gameMap.avgLevel, 1));
        if (opponent.getLevel() <= 0) opponent.setLevel(1);

        opponent.setStats();
    }

    /**
     * Handles and applies the damage/heal of a move to an Entity
     *
     * @param move
     * @return a string array for the dialog ui description
     */
    public String[] handleMove(Move move) {
        String[] dialog = null;

        // distract/enemy debuff
        if (buffs[Util.DISTRACT]) {
            opponent.statusEffects.addEffect(StatusEffect.DISTRACT);
            opponent.setAccuracy(opponent.getAccuracy() - Util.P_DISTRACT);
        }
        else opponent.setAccuracy(MathUtils.random(Util.ENEMY_MIN_ACCURACY, Util.ENEMY_MAX_ACCURACY));

        // for red reaper boss's 30% acc debuff passive
        boolean redReaperDebuff = false;
        if (opponent.isBoss()) redReaperDebuff = ((Boss) opponent).bossId == 1;

        // accounting for player accuracy or accuracy buff
        if (Util.isSuccess(player.getAccuracy() - (redReaperDebuff ? 40 : 0)) || buffs[Util.FOCUS]) {
            // accurate or wide
            if (move.type < 2) {
                int damage = MathUtils.random(Math.round(move.minDamage), Math.round(move.maxDamage));
                if (buffs[Util.INTIMIDATE]) damage *= Util.INTIMIDATE_MULT;
                if (buffs[Util.SACRIFICE]) damage *= psacrifice;

                if (buffs[Util.INVERT]) {
                    // for heal animation
                    player.useMove(3);
                    player.heal(damage);
                    player.stats.hpHealed += damage;
                    player.stats.updateMax(player.stats.maxHealSingleMove, damage);
                    cumulativeHealing += damage;

                    dialog = new String[] {
                            "Bạn đã dùng " + move.name + " (ngược)!",
                            "Bạn hồi " + damage + " máu!"
                    };
                }
                else {
                    player.useMove(move.type);
                    damage = reduceDamage(damage);
                    cumulativeDamage += damage;
                    player.stats.damageDealt += damage;
                    player.stats.updateMax(player.stats.maxDamageSingleHit, damage);

                    opponent.hit(damage);
                    dialog = new String[]{
                            "Bạn đã dùng " + move.name + "!",
                            "Gây " + damage + " sát thương lên " + opponent.getId() + "."
                    };
                }
            }
            // crit (3x damage if success)
            else if (move.type == 2) {
                int damage = Math.round(move.minDamage);
                int critChance;

                if (buffs[Util.INTIMIDATE]) damage *= Util.INTIMIDATE_MULT;
                if (buffs[Util.SACRIFICE]) damage *= psacrifice;
                if (buffs[Util.FOCUS]) critChance = move.crit + Util.P_FOCUS_CRIT;
                else critChance = move.crit;

                if (Util.isSuccess(critChance)) {
                    damage *= Util.CRIT_MULTIPLIER;
                    if (buffs[Util.INVERT]) {
                        player.useMove(3);
                        player.heal(damage);
                        player.stats.hpHealed += damage;
                        player.stats.updateMax(player.stats.maxHealSingleMove, damage);
                        cumulativeHealing += damage;

                        dialog = new String[] {
                                "Bạn đã dùng " + move.name + " (ngược)!",
                                "Đòn chí mạng!",
                                "Bạn hồi " + damage + " máu!"
                        };
                    }
                    else {
                        player.useMove(move.type);
                        damage = reduceDamage(damage);
                        cumulativeDamage += damage;
                        player.stats.damageDealt += damage;
                        player.stats.updateMax(player.stats.maxDamageSingleHit, damage);

                        opponent.hit(damage);
                        dialog = new String[]{
                                "Bạn đã dùng " + move.name + "!",
                                "Đòn chí mạng!",
                                "Gây " + damage + " sát thương lên " + opponent.getId() + "."
                        };
                    }
                } else {
                    if (buffs[Util.INVERT]) {
                        player.useMove(3);
                        player.heal(damage);
                        player.stats.hpHealed += damage;
                        player.stats.updateMax(player.stats.maxHealSingleMove, damage);
                        cumulativeHealing += damage;

                        dialog = new String[] {
                                "Bạn đã dùng " + move.name + " (ngược)!",
                                "Bạn hồi " + damage + " máu!"
                        };
                    }
                    else {
                        player.useMove(move.type);
                        damage = reduceDamage(damage);
                        cumulativeDamage += damage;
                        player.stats.damageDealt += damage;
                        player.stats.updateMax(player.stats.maxDamageSingleHit, damage);

                        opponent.hit(damage);
                        dialog = new String[]{
                                "Bạn đã dùng " + move.name + "!",
                                "Gây " + damage + " sát thương lên " + opponent.getId() + "."
                        };
                    }
                }
            }
            // heal + set dmg reduction for next turn
            else if (move.type == 3) {
                int heal = MathUtils.random(Math.round(move.minHeal), Math.round(move.maxHeal));
                if (buffs[Util.INVERT]) {
                    player.useMove(MathUtils.random(0, 2));
                    opponent.hit(heal);
                    cumulativeDamage += heal;
                    player.stats.damageDealt += heal;
                    player.stats.updateMax(player.stats.maxDamageSingleHit, heal);

                    dialog = new String[] {
                            "Bạn đã dùng " + move.name + " (ngược)!",
                            "Gây " + heal + " sát thương lên " + opponent.getId() + "."
                    };
                }
                else {
                    player.useMove(move.type);
                    playerRed = move.dmgReduction;
                    player.heal(heal);
                    cumulativeHealing += heal;
                    player.stats.hpHealed += heal;
                    player.stats.updateMax(player.stats.maxHealSingleMove, heal);
                    player.statusEffects.addEffect(StatusEffect.DMG_RED);

                    dialog = new String[]{
                            "Bạn đã dùng " + move.name + "!",
                            "Đòn đánh tiếp theo của địch -" + move.dmgReduction + "% sát thương!",
                            "Bạn được hồi " + heal + " máu."
                    };
                }
            }
        }
        else {
            player.stats.numMovesMissed++;
            // move missed; enemy turn
            dialog = new String[] {"Ôi không, đòn đánh của bạn đã hụt!"};
        }

        return dialog;
    }

    /**
     * Enemy picks a random move out of its random moveset
     *
     * @return the dialog of the enemy's move and damage
     */
    public String[] enemyTurn() {
        // skip turn if stunned
        if (buffs[Util.STUN]) {
            if (Util.isSuccess(Util.P_STUN)) {
                resetBuffs();
                opponent.statusEffects.addEffect(StatusEffect.STUN);
                return new String[] {
                        "Địch bị choáng và không thể di chuyển!"
                };
            }
        }

        // get special boss moves
        if (opponent.isBoss()) {
            opponent.getMoveset().reset(opponent.getMinDamage(), opponent.getMaxDamage(), opponent.getMaxHp(), ((Boss) opponent).bossId);
        }
        else {
            opponent.getMoveset().reset(opponent.getMinDamage(), opponent.getMaxDamage(), opponent.getMaxHp());
        }
        String[] dialog = null;
        Move move = opponent.getMoveset().moveset[MathUtils.random(3)];

        if (opponent.isBoss() || opponent.isElite()) {
            // when below 20% hp, elite and bosses will always try to go for heal moves as first priority
            if (opponent.healthBelow(20)) move = opponent.getMoveset().getHealPriority();
            // when player is below 20%, elite and bosses will always go for damage moves
            if (player.healthBelow(20)) move = opponent.getMoveset().getDamagePriority();
        }

        if (Util.isSuccess(opponent.getAccuracy())) {
            // enemy's attack is reflected back at itself
            if (buffs[Util.REFLECT]) {
                // elites and bosses will try to counter reflect by prioritizing heal moves
                if (opponent.isBoss() || opponent.isElite()) move = opponent.getMoveset().getHealPriority();

                // accurate or wide
                if (move.type < 2) {
                    player.useMove(move.type);
                    int damage = MathUtils.random(Math.round(move.minDamage), Math.round(move.maxDamage));
                    opponent.hit(damage);
                    dialog = new String[]{
                            opponent.getId() + " đã sử dụng " + move.name + "!",
                            "Phản đòn! Gây " + damage + " sát thương lên " + opponent.getId() + "!"
                    };
                }
                // crit (3x damage if success)
                else if (move.type == 2) {
                    player.useMove(move.type);
                    int damage = Math.round(move.minDamage);
                    if (Util.isSuccess(move.crit)) {
                        damage *= Util.CRIT_MULTIPLIER;
                        opponent.hit(damage);
                        dialog = new String[]{
                                opponent.getId() + " đã sử dụng " + move.name + "!",
                                "Đòn chí mạng!",
                                "Phản đòn! Gây " + damage + " sát thương lên " + opponent.getId() + "!"
                        };
                    } else {
                        opponent.hit(damage);
                        dialog = new String[] {
                                opponent.getId() + " đã sử dụng " + move.name + "!",
                                "Phản đòn! Gây " + damage + " damage to " + opponent.getId() + "!"
                        };
                    }
                }
                // heal gets doubled when reflected
                else if (move.type == 3) {
                    opponent.useMove(move.type);
                    int heal = MathUtils.random(Math.round(move.minHeal), Math.round(move.maxHeal));
                    heal *= 2;
                    enemyRed = move.dmgReduction;
                    opponent.heal(heal);
                    dialog = new String[]{
                            opponent.getId() + " đã sử dụng " + move.name + "!",
                            "Đòn tấn công tiếp theo của bạn -" + move.dmgReduction + "% sát thương!",
                            "Phản hồi! Địch được hồi máu nhiều hơn",
                            opponent.getId() + " được hồi " + heal + " máu."
                    };
                }
            }
            else {
                opponent.useMove(move.type);
                // accurate or wide
                if (move.type < 2) {
                    int damage = MathUtils.random(Math.round(move.minDamage), Math.round(move.maxDamage));
                    damage = reduceDamage(damage);
                    player.stats.damageTaken += damage;
                    player.hit(damage);
                    dialog = new String[]{
                            opponent.getId() + " đã sử dụng " + move.name + "!",
                            "Gây " + damage + " sát thương lên bạn."
                    };

                    // ice golem passive
                    if (opponent.isBoss()) {
                        if (((Boss) opponent).bossId == 2 && move.type == 0) {
                            int heal = (int) (0.2 * (float) damage);
                            opponent.heal(heal);
                            dialog = new String[] {
                                    opponent.getId() + " đã sử dụng " + move.name + "!",
                                    "gây " + damage + " sát thương lên bạn.",
                                    opponent.getId() + " hồi " + heal + " máu nhờ hút máu!"
                            };
                        }
                    }
                }
                // crit (3x damage if success)
                else if (move.type == 2) {
                    int damage = Math.round(move.minDamage);
                    if (Util.isSuccess(move.crit)) {
                        damage *= Util.CRIT_MULTIPLIER;
                        damage = reduceDamage(damage);
                        player.stats.damageTaken += damage;
                        player.hit(damage);
                        dialog = new String[]{
                                opponent.getId() + " dã sử dụng " + move.name + "!",
                                "Đòn chí mạng!",
                                "Gây " + damage + " sát thương lên bạn."
                        };
                    } else {
                        damage = reduceDamage(damage);
                        player.stats.damageTaken += damage;
                        player.hit(damage);
                        dialog = new String[]{
                                opponent.getId() + " đã sử dụng " + move.name + "!",
                                "Gây " + damage + " sát thương lên bạn."
                        };
                    }
                }
                // heal
                else if (move.type == 3) {
                    int heal = MathUtils.random(Math.round(move.minHeal), Math.round(move.maxHeal));
                    enemyRed = move.dmgReduction;
                    opponent.heal(heal);
                    opponent.statusEffects.addEffect(StatusEffect.DMG_RED);
                    dialog = new String[]{
                            opponent.getId() + " đã sử dụng " + move.name + "!",
                            "Đòn tấn công tiếp theo của bạn -" + move.dmgReduction + "% sát thương!",
                            opponent.getId() + " được hồi " + heal + " máu."
                    };
                }
            }
        }
        else {
            dialog = new String[] { opponent.getId() + " hụt chiêu"};
        }

        // only reset buffs that don't affect enemy's turn
        if (!buffs[Util.REFLECT]) resetBuffs();

        return dialog;
    }

    /**
     * Reduces the damage of an entity by the heal damage reduction
     *
     * @param damage
     * @return
     */
    public int reduceDamage(int damage) {
        int dmg = damage;
        if (playerRed != -1) {
            dmg -= ((playerRed / 100f) * damage);
            playerRed = -1;
        }
        else if (enemyRed != -1) {
            dmg -= ((enemyRed / 100f) * damage);
            enemyRed = -1;
        }
        return dmg;
    }

    /**
     * Returns the dialogs associated with each special move
     *
     * @param index
     * @return
     */
    public String[] getSpecialMoveDialog(int index) {
        switch (index) {
            case Util.DISTRACT:
                return new String[] {
                        "Hất bụi vào mặt địch.",
                        "Đòn tiếp theo của địch -" + Util.P_DISTRACT + "% độ chính xác!"
                };
            case Util.FOCUS:
                return new String[] {
                        "Vận sức!",
                        "Đòn tiếp theo chắc chắn trúng, tỉ lệ chí mạng tăng " + Util.P_FOCUS_CRIT + "%!"
                };
            case Util.INTIMIDATE:
                return new String[] {
                        "Đe dọa kẻ địch!",
                        "Giảm phòng thủ của địch. Đòn tiếp theo +" + Util.P_INTIMIDATE + "% sát thương."
                };
            case Util.REFLECT:
                return new String[] {
                        "Tập trung đối phó!",
                        "Đòn tiếp theo của địch phản lại chính nó!"
                };
            case Util.STUN:
                return new String[] {
                        "Thôi miên kẻ địch!",
                        "Địch có thể bị choáng"
                };
            case Util.INVERT:
                return new String[] {
                        "Thao túng sức mạnh!",
                        "Trong 1 lượt: chiêu hồi máu sẽ gây sát thương, chiêu gây sát thương sẽ hồi máu!"
                };
            case Util.SACRIFICE:
                return new String[] {
                        "Hiến tế toàn bộ!",
                        "Đòn tấn công tiếp theo +" + (int) Math.ceil(((player.getHp() - 1) / (float) player.getMaxHp()) * 100) +
                                "% sát thương!"
                };
            case Util.SHIELD:
                return new String[] {
                        "Dồn sức phòng thủ.",
                        "Tạo lá chắn hấp thụ " + (int) ((Util.P_SHIELD / 100f) * (float) player.getMaxHp()) + " sát thương của kẻ địch!"
                };
        }
        return null;
    }

    /**
     * 1-3 extra exp from enemy to balance exp growth
     * Elite monsters give 1.5x exp and bosses give 3x exp
     *
     * @return
     */
    public int getBattleExp() {
        if (opponent.isElite())
            return (int) (1.5 * Util.calculateExpEarned(opponent.getLevel(), MathUtils.random(2) + 1));
        else if (opponent.isBoss())
            return (3 * Util.calculateExpEarned(opponent.getLevel(), MathUtils.random(2) + 1));
        else
            return Util.calculateExpEarned(opponent.getLevel(), MathUtils.random(2));
    }

    /**
     * Gold earned scales off enemy level and player level
     * The player will receive less gold the greater the level difference and vice versa
     * (player.level - enemy.level)
     *
     * @return
     */
    public int getGoldGained() {
        int gold = 0;
        int diff = player.getLevel() - opponent.getLevel();

        for (int i = 0; i < opponent.getLevel(); i++) {
            gold += MathUtils.random(2) + 1;
        }
        gold -= (opponent.getLevel() * diff);
        if (gold <= 0) gold = 1;

        return gold;
    }

    /**
     * Handles the probabilities of item dropping from enemies and
     * returns the Item that they drop
     * Returns null if the enemy doesn't drop an item
     *
     * @param rm
     * @return
     */
    public Item getItemObtained(ResourceManager rm) {
        if (opponent.isElite()) {
            if (Util.isSuccess(Util.ELITE_ITEM_DROP)) {
                // elite will drop rare, epic, and legendary items at 60/30/10 chances
                int k = MathUtils.random(99);
                // rare
                if (k < 60) {
                    return rm.getItem(1, opponent.getLevel());
                }
                else if (k < 90) {
                    return rm.getItem(2, opponent.getLevel());
                }
                else if (k < 100) {
                    return rm.getItem(3, opponent.getLevel());
                }
            }
        }
        else if (opponent.isBoss()) {
            if (Util.isSuccess(Util.BOSS_ITEM_DROP)) {
                // boss will only drop epic and legendary items at 70/30 chances
                int k = MathUtils.random(99);
                // epic
                if (k < 70) {
                    return rm.getItem(2, opponent.getLevel());
                }
                // legendary
                else {
                    return rm.getItem(3, opponent.getLevel());
                }
            }
        }
        else {
            if (Util.isSuccess(Util.NORMAL_ITEM_DROP)) {
                return rm.getRandomItem(opponent.getLevel());
            }
        }
        return null;
    }

    public String getItemDialog(Item item) {
        String ret = "";

        // enemy didn't drop an item
        if (item == null) {
            ret = "Kẻ địch không rơi vật phẩm nào.";
        }
        else {
            // if the player's inventory is full then he cannot obtain the item
            if (player.inventory.isFull()) {
                ret = "Kẻ địch không thể rơi vật phẩm vì túi đồ của bạn đã đầy.";
            }
            else {
                ret = "Kẻ địch rơi " + item.getDialogName() + "! " +
                        "Vật phẩm đã thêm vào túi";
                // scale item stats to match enemy level
                item.adjust(opponent.getLevel());
                player.inventory.addItem(item);
                gameScreen.gameMap.itemsObtained.add(item);
            }
        }

        return ret;
    }

    /**
     * Returns back to the map state
     */
    public void end() {
        opponent = null;
        tileMap.removeEntity(tileMap.toTileCoords(player.getPosition()));
        player.finishBattling();
        gameScreen.setCurrentEvent(EventState.MOVING);
        gameScreen.hud.toggle(true);
    }

}
