package ru.megazlo.apnea.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@DatabaseTable(tableName = "table_apnea")
public class TableApnea extends AbstractEntity {

	private boolean running = false;

	private TableType type = TableType.USER;

	@DatabaseField(canBeNull = false, columnName = "allow_edit")
	private boolean allowEdit;

	@DatabaseField(canBeNull = false, columnName = "color")
	private int color = 0xff0e71ae;

	@DatabaseField(canBeNull = false, columnName = "title")
	private String title;

	@DatabaseField(columnName = "description")
	private String description;

	private List<TableApneaRow> rows;

	public TableApnea(Integer id) {
		setId(id);
	}
}
