package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.enums.TransferStatusEnum;
import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.TransferDto;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDao implements TransferDao {

    private final JdbcTemplate jdbcTemplate;

    public JdbcTransferDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<TransferDto> listHistoryByUserId(Integer userId) {
        List<TransferDto> transferDtoList = new ArrayList<>();

        try {
            String sql = listSql()
                    + " where (ta.user_id = ? "
                    + " or fa.user_id = ? ) "
                    + " order by t.transfer_id ";
            SqlRowSet result = jdbcTemplate.queryForRowSet(sql, userId, userId);
            while (result.next()) {
                TransferDto transferDto = mapRowToTransfer(result);
                transferDtoList.add(transferDto);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return transferDtoList;
    }

    public List<TransferDto> listPendingByUserId(Integer userId) {
        List<TransferDto> transferDtoList = new ArrayList<>();
        String sql = listSql()
                +" where ta.user_id=? "
                + "and t.transfer_status_id= ?"
                + "order by t.transfer_id ";
        try {
            SqlRowSet result = jdbcTemplate.queryForRowSet(sql, userId, TransferStatusEnum.PENDING.getStatusId());
            while (result.next()) {
                TransferDto transferDto = mapRowToTransfer(result);
                transferDtoList.add(transferDto);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return transferDtoList;
    }



    private String listSql() {
        return
                "select t.transfer_id,t.transfer_type_id,t.transfer_status_id,t.account_from,fu.username as accountFromName,account_to,tu.username as accountToName,t.amount " +
                        "from transfer t " +
                        "left join account fa on t.account_from = fa.account_id " +
                        "left join tenmo_user fu on fa.user_id = fu.user_id " +
                        "left join account ta on t.account_to = ta.account_id " +
                        "left join tenmo_user tu on ta.user_id = tu.user_id " ;
    }

    @Override
    public void updateTransferStatus(Integer transferId, Integer newStatusId) {
        String sql = "UPDATE transfer SET transfer_status_id = ? WHERE transfer_id = ?";
        try {
            jdbcTemplate.update(sql, newStatusId, transferId);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
    }


    @Override
    public TransferDto getTransferById(Integer transferId) {
        String sql = "SELECT t.transfer_id, t.transfer_type_id, t.transfer_status_id, t.account_from, " +
                "fu.username as accountFromName, t.account_to, tu.username as accountToName, tu.user_id as userIdTo,  t.amount " +
                "FROM transfer t " +
                "LEFT JOIN account fa ON t.account_from = fa.account_id " +
                "LEFT JOIN tenmo_user fu ON fa.user_id = fu.user_id " +
                "LEFT JOIN account ta ON t.account_to = ta.account_id " +
                "LEFT JOIN tenmo_user tu ON ta.user_id = tu.user_id " +
                "WHERE t.transfer_id = ?";

        try {
            SqlRowSet result = jdbcTemplate.queryForRowSet(sql, transferId);
            if (result.next()) {
                return mapRowToTransfer(result);
            } else {
                return null;
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
    }


    @Override
    public Integer addTransfer(Integer transferTypeId, Integer transferStatusId, Integer accountFrom, Integer accountTo, Double amount) {
        String sql = "INSERT INTO  transfer (transfer_type_id,transfer_status_id,account_from,account_to,amount) " +
                "VALUES(?,?,?,?,?) " +
                "RETURNING transfer_id";
        Integer transferId;
        try {
            transferId = jdbcTemplate.queryForObject(sql, Integer.class, transferTypeId, transferStatusId, accountFrom, accountTo, amount);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return transferId;
    }

    private TransferDto mapRowToTransfer(SqlRowSet result) {
        TransferDto transferDto = new TransferDto();
        transferDto.setTransferId(result.getInt("transfer_id"));
        transferDto.setTransferTypeId(result.getInt("transfer_type_id"));
        transferDto.setTransferStatusId(result.getInt("transfer_status_id"));
        transferDto.setAccountFrom(result.getInt("account_from"));
        transferDto.setAccountFromName(result.getString("accountFromName"));
        transferDto.setAccountToName(result.getString("accountToName"));
        transferDto.setAccountTo(result.getInt("account_to"));
        transferDto.setAmount(result.getDouble("amount"));

        return transferDto;
    }
}
